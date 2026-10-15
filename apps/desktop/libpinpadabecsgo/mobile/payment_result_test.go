package mobile

import (
	"bytes"
	"encoding/json"
	"sync"
	"testing"
)

func TestPaymentResultCopiesClearAndNoJSON(t *testing.T) {
	r := &PaymentResult{decision: "202000", pinBlock: []byte{1, 2, 3, 4, 5, 6, 7, 8}, ksn: []byte{0, 1, 2, 3, 4, 5, 6, 7, 8, 9}}
	block, ksn := r.PINBlock(), r.KSN()
	block[0], ksn[0] = 99, 99
	if r.PINBlock()[0] != 1 || r.KSN()[0] != 0 {
		t.Fatal("buffer aliased")
	}
	data, err := json.Marshal(r)
	if err != nil || string(data) != "{}" {
		t.Fatal("private result serialized")
	}
	var wg sync.WaitGroup
	wg.Go(func() {
		for range 100 {
			_ = r.PINBlock()
			_ = r.KSN()
			_ = r.Result()
		}
	})
	wg.Go(func() { r.Clear(); r.Clear() })
	wg.Wait()
	if len(r.PINBlock()) != 0 || len(r.KSN()) != 0 || r.Result() != "202000" {
		t.Fatal("clear/result contract")
	}
	var empty *PaymentResult
	empty.Clear()
	if empty.Result() != "" || empty.PINBlock() != nil || empty.KSN() != nil {
		t.Fatal("nil result")
	}
}

func TestPaymentPINConfigurationComesFromWholeSource(t *testing.T) {
	for _, index := range []string{"17", "63"} {
		config, err := ParsePaymentPINConfiguration("TIPO_CHAVE=3\nINDICE_CHAVE=" + index + "\nWORKING_KEY=\"\"\n")
		if err != nil {
			t.Fatal(err)
		}
		wanted := 17
		if index == "63" {
			wanted = 63
		}
		if config.Method() != "3" || config.KeyIndex() != wanted || config.WorkingKey() != "" {
			t.Fatal("source snapshot not preserved")
		}
	}
	for _, raw := range []string{"TIPO_CHAVE=3\nINDICE_CHAVE=17", "TIPO_CHAVE=1\nINDICE_CHAVE=17\nWORKING_KEY=SECRET_SENTINEL"} {
		value, err := ParsePaymentPINConfiguration(raw)
		if err == nil || value != nil || bytes.Contains([]byte(err.Error()), []byte("SECRET_SENTINEL")) {
			t.Fatal("invalid source accepted/exposed")
		}
	}
}

func TestPrivateResultCannotSucceedWithoutSession(t *testing.T) {
	c, err := NewClient("127.0.0.1", 1, 100)
	if err != nil {
		t.Fatal(err)
	}
	if result, err := c.CapturePaymentPINResult("id", "3", 17, "", "PIN"); err == nil || result != nil {
		t.Fatal("PIN without context")
	}
	if result, err := c.ContinuePaymentEMVResult("id", "01", "000000000100", "3", 17, "", 30); err == nil || result != nil {
		t.Fatal("EMV without context")
	}
	if result, err := c.ContinuePaymentEMVResult("id", "01", "000000000100", "1", 17, "", 30); err == nil || result != nil {
		t.Fatal("invalid method")
	}
	if result, err := c.ContinuePaymentEMVResult("id", "01", "invalid", "3", 17, "", 30); err == nil || result != nil {
		t.Fatal("invalid amount")
	}
}
