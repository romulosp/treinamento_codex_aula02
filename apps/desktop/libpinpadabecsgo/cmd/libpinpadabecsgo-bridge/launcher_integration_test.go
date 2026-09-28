//go:build windows && integration

package main

import (
	"os"
	"os/exec"
	"path/filepath"
	"strings"
	"testing"
)

func fixtureTools(t *testing.T) string {
	t.Helper()
	dir := filepath.Join(t.TempDir(), "fixture com espacos")
	if err := os.Mkdir(dir, 0700); err != nil {
		t.Fatal(err)
	}
	cmd := exec.Command("go", "build", "-o", filepath.Join(dir, "go.exe"), "./testdata/launcher")
	if output, err := cmd.CombinedOutput(); err != nil {
		t.Fatalf("fixture: %s %v", output, err)
	}
	data, err := os.ReadFile(filepath.Join(dir, "go.exe"))
	if err != nil {
		t.Fatal(err)
	}
	if err := os.WriteFile(filepath.Join(dir, "adb.exe"), data, 0600); err != nil {
		t.Fatal(err)
	}
	return dir
}

func fixtureEnvironment(tools string, overrides map[string]string) []string {
	overrides["PATH"] = tools + ";" + os.Getenv("SystemRoot") + "\\System32;" + os.Getenv("SystemRoot") + "\\System32\\WindowsPowerShell\\v1.0"
	for _, key := range []string{"PORTA_PINPAD", "PINPAD_BAUDRATE", "PINPAD_TIMEOUT", "PINPAD_BRIDGE_PORT", "PINPAD_LOG_FILE", "PINPAD_BRIDGE_TRANSPORT", "ANDROID_SERIAL", "ANDROID_HOME", "ANDROID_SDK_ROOT"} {
		if _, ok := overrides[key]; !ok {
			overrides[key] = ""
		}
	}
	env := []string{}
	for _, value := range os.Environ() {
		key, _, _ := strings.Cut(value, "=")
		if _, ok := overrides[strings.ToUpper(key)]; !ok {
			env = append(env, value)
		}
	}
	for key, value := range overrides {
		env = append(env, key+"="+value)
	}
	return env
}

func TestLauncherPreservesEnvironmentAndExitCode(t *testing.T) {
	tools := fixtureTools(t)
	root, err := findModuleRoot(".")
	if err != nil {
		t.Fatal(err)
	}
	for _, tc := range []struct {
		name, port, exit string
		wantCode         int
	}{
		{"COM14", "COM14", "0", 0}, {"otherCOM", "COM22", "17", 17}, {"missing", "", "0", 1}, {"spaces", "   ", "0", 1},
	} {
		t.Run(tc.name, func(t *testing.T) {
			cmd := exec.Command("cmd.exe", "/d", "/c", filepath.Join(root, "testar_bridge_pinpad.bat"))
			cmd.Env = fixtureEnvironment(tools, map[string]string{"PORTA_PINPAD": tc.port, "PINPAD_BAUDRATE": "38400", "PINPAD_TIMEOUT": "45", "PINPAD_BRIDGE_PORT": "39272", "PINPAD_BRIDGE_TRANSPORT": "scripted", "FAKE_ADB_CASE": "single", "FAKE_ADB_PORT": "39272", "FAKE_GO_EXIT": tc.exit})
			cmd.Stdin = strings.NewReader("\n")
			output, _ := cmd.CombinedOutput()
			if cmd.ProcessState.ExitCode() != tc.wantCode {
				t.Fatalf("exit=%d: %s", cmd.ProcessState.ExitCode(), output)
			}
			if tc.wantCode != 1 {
				for _, want := range []string{"fake_go PORTA_PINPAD=" + tc.port, "fake_go PINPAD_BAUDRATE=38400", "fake_go PINPAD_TIMEOUT=45", "fake_go PINPAD_BRIDGE_PORT=39272", "fake_go PINPAD_BRIDGE_TRANSPORT=", "emulator_prepared"} {
					if !strings.Contains(string(output), want) {
						t.Fatalf("missing %s: %s", want, output)
					}
				}
			} else if strings.Contains(string(output), "fake_go") {
				t.Fatal("invalid PORTA_PINPAD started Go")
			}
		})
	}
}

func TestEmulatorHelperSelectsOnlyOnlineEmulatorAndChecksReverse(t *testing.T) {
	tools := fixtureTools(t)
	root, err := findModuleRoot(".")
	if err != nil {
		t.Fatal(err)
	}
	for _, tc := range []struct {
		name, mode, serial string
		wantCode           int
	}{
		{"single", "single", "", 0}, {"zero", "zero", "", 1}, {"ambiguous", "multiple", "", 1},
		{"explicit", "multiple", "emulator-5556", 0}, {"offline", "offline", "emulator-5554", 1},
		{"physical", "single", "physical-device", 1}, {"reverse_fail", "reverse_fail", "", 1}, {"list_fail", "list_fail", "", 1},
	} {
		t.Run(tc.name, func(t *testing.T) {
			cmd := exec.Command("powershell.exe", "-NoProfile", "-ExecutionPolicy", "Bypass", "-File", filepath.Join(root, "preparar_emulador.ps1"), "-BridgePort", "39272")
			cmd.Env = fixtureEnvironment(tools, map[string]string{"FAKE_ADB_CASE": tc.mode, "FAKE_ADB_PORT": "39272", "ANDROID_SERIAL": tc.serial})
			output, _ := cmd.CombinedOutput()
			if cmd.ProcessState.ExitCode() != tc.wantCode {
				t.Fatalf("exit=%d: %s", cmd.ProcessState.ExitCode(), output)
			}
			if tc.wantCode == 0 && !strings.Contains(string(output), "emulator_prepared") {
				t.Fatal(string(output))
			}
		})
	}
}
