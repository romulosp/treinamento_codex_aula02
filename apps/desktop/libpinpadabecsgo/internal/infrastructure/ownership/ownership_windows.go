//go:build windows

package ownership

import (
	"context"
	"errors"
	"fmt"
	"runtime"
	"sync"
	"syscall"
	"unsafe"
)

const (
	waitObject0   = 0x00000000
	waitAbandoned = 0x00000080
	waitTimeout   = 0x00000102
)

var (
	kernel32            = syscall.NewLazyDLL("kernel32.dll")
	createMutex         = kernel32.NewProc("CreateMutexW")
	waitForSingleObject = kernel32.NewProc("WaitForSingleObject")
	releaseMutex        = kernel32.NewProc("ReleaseMutex")
	closeHandle         = kernel32.NewProc("CloseHandle")
)

// acquirePlatform mantém aquisição e release na mesma thread Windows. O worker
// pertence ao callback de release; não pode migrar entre threads durante a posse.
func acquirePlatform(ctx context.Context, resource string) (func() error, error) {
	name, err := syscall.UTF16PtrFromString("Local\\lib-pinpad-abecs-" + resource)
	if err != nil {
		return nil, fmt.Errorf("create ownership name: %w", err)
	}
	ready := make(chan error, 1)
	request, finished := make(chan struct{}), make(chan error, 1)
	go func() {
		runtime.LockOSThread()
		defer runtime.UnlockOSThread()
		handle, _, callErr := createMutex.Call(0, 0, uintptr(unsafe.Pointer(name)))
		if handle == 0 {
			ready <- fmt.Errorf("create ownership mutex: %w", callErr)
			return
		}
		closeOwned := func(release bool) error {
			var result error
			if release {
				if ok, _, err := releaseMutex.Call(handle); ok == 0 {
					result = fmt.Errorf("release ownership mutex: %w", err)
				}
			}
			if ok, _, err := closeHandle.Call(handle); ok == 0 {
				result = errors.Join(result, fmt.Errorf("close ownership handle: %w", err))
			}
			return result
		}
		if err := ctx.Err(); err != nil {
			ready <- errors.Join(err, closeOwned(false))
			return
		}
		status, _, waitErr := waitForSingleObject.Call(handle, 0)
		switch status {
		case waitAbandoned:
			ready <- errors.Join(ErrAbandoned, closeOwned(true))
			return
		case waitTimeout:
			ready <- errors.Join(ErrBusy, closeOwned(false))
			return
		case waitObject0:
			if err := ctx.Err(); err != nil {
				ready <- errors.Join(err, closeOwned(true))
				return
			}
		default:
			ready <- errors.Join(fmt.Errorf("wait ownership mutex: %w", waitErr), closeOwned(false))
			return
		}
		ready <- nil
		<-request
		finished <- closeOwned(true)
	}()
	if err := <-ready; err != nil {
		return nil, err
	}
	release := sync.OnceValue(func() error { close(request); return <-finished })
	if err := ctx.Err(); err != nil {
		return nil, errors.Join(err, release())
	}
	return release, nil
}
