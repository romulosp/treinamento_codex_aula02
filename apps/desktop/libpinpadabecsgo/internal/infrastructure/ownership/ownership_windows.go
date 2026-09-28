//go:build windows

package ownership

import (
	"context"
	"errors"
	"fmt"
	"syscall"
	"unsafe"
)

const (
	waitObject0 = 0x00000000
	waitTimeout = 0x00000102
)

var (
	kernel32            = syscall.NewLazyDLL("kernel32.dll")
	createMutex         = kernel32.NewProc("CreateMutexW")
	waitForSingleObject = kernel32.NewProc("WaitForSingleObject")
	releaseMutex        = kernel32.NewProc("ReleaseMutex")
	closeHandle         = kernel32.NewProc("CloseHandle")
)

func acquirePlatform(ctx context.Context, resource string) (func() error, error) {
	name, err := syscall.UTF16PtrFromString("Local\\lib-pinpad-abecs-" + resource)
	if err != nil {
		return nil, fmt.Errorf("create ownership name: %w", err)
	}
	handle, _, callErr := createMutex.Call(0, 0, uintptr(unsafe.Pointer(name)))
	if handle == 0 {
		return nil, fmt.Errorf("create ownership mutex: %w", callErr)
	}
	if err := ctx.Err(); err != nil {
		_, _, _ = closeHandle.Call(handle)
		return nil, err
	}
	result, _, waitErr := waitForSingleObject.Call(handle, 0)
	switch result {
	case waitObject0:
		return func() error {
			releaseResult, _, releaseErr := releaseMutex.Call(handle)
			closeResult, _, closeErr := closeHandle.Call(handle)
			if releaseResult == 0 || closeResult == 0 {
				if releaseErr != nil || closeErr != nil {
					return fmt.Errorf("release serial ownership: %v: %v", releaseErr, closeErr)
				}
				return errors.New("release serial ownership")
			}
			return nil
		}, nil
	case waitTimeout:
		_, _, _ = closeHandle.Call(handle)
		return nil, ErrBusy
	default:
		_, _, _ = closeHandle.Call(handle)
		if waitErr != nil {
			return nil, fmt.Errorf("wait serial ownership: %w", waitErr)
		}
		return nil, ErrBusy
	}
}
