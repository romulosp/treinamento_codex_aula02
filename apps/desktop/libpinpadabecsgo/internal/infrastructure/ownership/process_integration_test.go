//go:build windows && integration

package ownership

import (
	"bufio"
	"context"
	"errors"
	"fmt"
	"os"
	"os/exec"
	"runtime"
	"syscall"
	"testing"
	"unsafe"
)

func TestOwnershipProcessHelper(t *testing.T) {
	mode := os.Getenv("OWNERSHIP_TEST_MODE")
	if mode == "" {
		return
	}
	release, err := New().Acquire(context.Background(), os.Getenv("OWNERSHIP_RESOURCE"))
	if err != nil {
		fmt.Println("error")
		os.Exit(2)
	}
	fmt.Println("held")
	if mode == "abandon" {
		os.Exit(0)
	}
	bufio.NewScanner(os.Stdin).Scan()
	if err := release(); err != nil {
		os.Exit(3)
	}
	if err := release(); err != nil {
		os.Exit(4)
	}
}

func startOwnerProcess(t *testing.T, resource, mode string) (*exec.Cmd, func()) {
	t.Helper()
	cmd := exec.Command(os.Args[0], "-test.run=^TestOwnershipProcessHelper$")
	cmd.Env = append(os.Environ(), "OWNERSHIP_TEST_MODE="+mode, "OWNERSHIP_RESOURCE="+resource)
	stdout, err := cmd.StdoutPipe()
	if err != nil {
		t.Fatal(err)
	}
	stdin, err := cmd.StdinPipe()
	if err != nil {
		t.Fatal(err)
	}
	if err := cmd.Start(); err != nil {
		t.Fatal(err)
	}
	scanner := bufio.NewScanner(stdout)
	if !scanner.Scan() || scanner.Text() != "held" {
		t.Fatal("child failed acquiring mutex")
	}
	return cmd, func() { _, _ = stdin.Write([]byte("release\n")); _ = stdin.Close() }
}

func TestWindowsOwnershipAcrossProcessesAndIdempotentRelease(t *testing.T) {
	resource := fmt.Sprintf("REGRESSION-072-%d", os.Getpid())
	cmd, releaseChild := startOwnerProcess(t, resource, "hold")
	defer releaseChild()
	if _, err := New().Acquire(context.Background(), resource); !errors.Is(err, ErrBusy) {
		t.Fatalf("second owner: %v", err)
	}
	releaseChild()
	if err := cmd.Wait(); err != nil {
		t.Fatal(err)
	}
	release, err := New().Acquire(context.Background(), resource)
	if err != nil {
		t.Fatal(err)
	}
	runtime.Gosched()
	if err := release(); err != nil {
		t.Fatal(err)
	}
	if err := release(); err != nil {
		t.Fatal(err)
	}
}

func TestWindowsAbandonedMutexIsReleasedAndAllowsExplicitRetry(t *testing.T) {
	resource := fmt.Sprintf("ABANDON-072-%d", os.Getpid())
	name, _ := syscall.UTF16PtrFromString("Local\\lib-pinpad-abecs-" + resource)
	handle, _, err := createMutex.Call(0, 0, uintptr(unsafe.Pointer(name)))
	if handle == 0 {
		t.Fatal(err)
	}
	defer closeHandle.Call(handle)
	cmd, _ := startOwnerProcess(t, resource, "abandon")
	if err := cmd.Wait(); err != nil {
		t.Fatal(err)
	}
	if _, err := New().Acquire(context.Background(), resource); !errors.Is(err, ErrAbandoned) {
		t.Fatalf("abandoned = %v", err)
	}
	release, err := New().Acquire(context.Background(), resource)
	if err != nil {
		t.Fatal(err)
	}
	if err := release(); err != nil {
		t.Fatal(err)
	}
}
