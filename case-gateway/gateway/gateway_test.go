package main

import (
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
	"io/ioutil"
	"log"
	"net/http"
	"net/url"
	"testing"
)

var fs = http.FileServer(http.Dir("./static"))

func serveFolder(path string, host string) {
	r1 := http.NewServeMux()
	fs := http.FileServer(http.Dir(path))
	r1.Handle("/", fs)

	err := http.ListenAndServe(host, r1)
	if err != nil {
		log.Fatal(err)
	}
}

func init() {
	// CCD
	const ccdUrl = "localhost:6000"
	const indieUrl = "localhost:7000"
	go serveFolder("static/ccd-responses", ccdUrl)
	go serveFolder("static/independent-responses", indieUrl)
	go start("localhost:9650", ccdUrl, indieUrl)
}

func CheckRequest(t *testing.T, expectedRoot string, u string) {
	res, _ := http.Get("http://localhost:9650" + u)
	body, _ := ioutil.ReadAll(res.Body)

	p, _ := url.Parse(u)
	expected, err := ioutil.ReadFile("static/" + expectedRoot + p.Path)
	if err != nil {
		panic(err)
	}
	require.JSONEq(t, string(expected), string(body))
}

func TestGetJurisdictions(t *testing.T) {
	const expectedRoot = "expected-responses"
	resource := "/aggregated/caseworkers/:uid/jurisdictions"
	CheckRequest(t, expectedRoot, resource)
}

func TestRoutesDivorceWBI(t *testing.T) {
	const expectedRoot = "ccd-responses"
	resource := "/data/internal/case-types/DIVORCE/work-basket-inputs"
	CheckRequest(t, expectedRoot, resource)
}

func TestRoutesNFDWBI(t *testing.T) {
	const expectedRoot = "independent-responses"
	resource := "/data/internal/case-types/NFD/work-basket-inputs"
	CheckRequest(t, expectedRoot, resource)
}

func TestRoutesNFDSearch(t *testing.T) {
	const expectedRoot = "independent-responses"
	resource := "/data/internal/searchCases?ctid=NFD&use_case=WORKBASKET&view=WORKBASKET&state=Open&page=1"
	CheckRequest(t, expectedRoot, resource)
}

func TestRoutesCCDCase(t *testing.T) {
	const expectedRoot = "ccd-responses"
	resource := "/data/internal/cases/1515433003120937"
	CheckRequest(t, expectedRoot, resource)
}

func TestRoutesIndieCase(t *testing.T) {
	const expectedRoot = "independent-responses"
	resource := "/data/internal/cases/2515433003120937"
	CheckRequest(t, expectedRoot, resource)
}

func TestRoutesValidation(t *testing.T) {
	const expectedRoot = "independent-responses"
	resource := "/data/case-types/2542345663454321/validate"
	CheckRequest(t, expectedRoot, resource)
}

func TestRoutesEvents(t *testing.T) {
	u, _ := url.Parse("http://localhost:3455/data/cases/2542345663454321/events")
	assert.True(t, isIndie(u))
}

func TestHealthy(t *testing.T) {
	res, _ := http.Get("http://localhost:9650/health")
	assert.Equal(t, 200, res.StatusCode)
}

func TestHandlesIndieDown(t *testing.T) {
	IndependentHost = "localhost:32"
	const expectedRoot = "ccd-responses"
	resource := "/aggregated/caseworkers/:uid/jurisdictions"
	CheckRequest(t, expectedRoot, resource)
}
