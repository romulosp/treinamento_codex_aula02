// Comando substituto dos testes de integração: não acessa pinpad nem ADB real.
package main

import (
	"fmt"
	"os"
	"path/filepath"
	"strconv"
	"strings"
)

func main() {
	if strings.EqualFold(filepath.Base(os.Args[0]), "adb.exe") {
		args := os.Args[1:]
		mode := os.Getenv("FAKE_ADB_CASE")
		if len(args) == 1 && args[0] == "devices" {
			fmt.Println("List of devices attached")
			switch mode {
			case "single", "multiple", "reverse_fail", "list_fail":
				fmt.Println("emulator-5554\tdevice")
			case "offline":
				fmt.Println("emulator-5554\toffline")
			}
			if mode == "multiple" {
				fmt.Println("emulator-5556\tdevice")
			}
			fmt.Println("physical-device\tdevice")
			return
		}
		if len(args) >= 4 && args[0] == "-s" && args[2] == "reverse" {
			port := os.Getenv("FAKE_ADB_PORT")
			if port == "" {
				port = "39100"
			}
			if args[3] == "--list" {
				if mode != "list_fail" {
					fmt.Printf("UsbFfs tcp:%s tcp:%s\n", port, port)
				}
				return
			}
			if mode == "reverse_fail" || len(args) != 5 || args[3] != "tcp:"+port || args[4] != "tcp:"+port {
				os.Exit(2)
			}
			fmt.Printf("fake_reverse serial=%s port=%s\n", args[1], port)
			return
		}
		os.Exit(3)
	}
	for _, key := range []string{"PORTA_PINPAD", "PINPAD_BAUDRATE", "PINPAD_TIMEOUT", "PINPAD_BRIDGE_PORT", "PINPAD_BRIDGE_TRANSPORT"} {
		fmt.Printf("fake_go %s=%s\n", key, os.Getenv(key))
	}
	code, _ := strconv.Atoi(os.Getenv("FAKE_GO_EXIT"))
	os.Exit(code)
}
