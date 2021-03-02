package main

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"log"
	"net/http"
	"net/http/httputil"
	"net/url"
	"os"
	"regexp"
	"strings"
)


func serveReverseProxy(target string, res http.ResponseWriter, req *http.Request) {
	// parse the url
	url, _ := url.Parse(target)

	// create the reverse proxy
	proxy := httputil.NewSingleHostReverseProxy(url)

	proxy.ServeHTTP(res, req)
}

func copyHeader(dst, src http.Header) {
	for k, vv := range src {
		for _, v := range vv {
			// Don't set accept encoding or it disables auto gzip decompression
			if !strings.Contains(strings.ToLower(k), "accept-encoding") {
				dst.Add(k, v)
			}
		}
	}
}

func fetchJsonArray(host string, req *http.Request) []interface{} {
	url := fmt.Sprintf("http://%s%s", host, req.RequestURI)
	proxyReq, _ := http.NewRequest(req.Method, url, nil)

	proxyReq.Header = make(http.Header)
	copyHeader(proxyReq.Header, req.Header)

	client := &http.Client{
	}
	resp, err := client.Do(proxyReq)
	if err != nil {
		log.Println(host)
		log.Println(err)
		var res []interface{}
		return res
	}

	data, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		log.Print(err)
		var res []interface{}
		return res
	}
	var arr []interface{}

	err = json.Unmarshal(data, &arr)
	if err != nil {
		log.Print(string(data))
		log.Print(err)
		var res []interface{}
		return res
	}
	return arr
}

func isIndie(url *url.URL) bool {
	keys, ok := url.Query()["ctid"]
	if ok && len(keys[0]) > 0 {
		if strings.ToLower(keys[0]) == "nfd" {
			return true
		}
	}

	// TODO: range based routing
	match, _ := regexp.MatchString("25\\d{14}", url.Path)
	if match {
		return true
	}

	if strings.Contains(strings.ToLower(url.Path), "/nfd") {
		return true
	}
	return false
}

// Given a request send it to the appropriate url
func handleRequestAndRedirect(res http.ResponseWriter, req *http.Request) {
	path := strings.ToLower(req.URL.Path)
	if strings.Compare(path, "/aggregated/caseworkers/:uid/jurisdictions") == 0 {
		first := fetchJsonArray(CcdHost, req)
		second := fetchJsonArray(IndependentHost, req)
		result := append(first, second...)
		json.NewEncoder(res).Encode(result)
		return
	}

	if strings.Compare(path, "/health") == 0 {
		fmt.Fprintf(res, "OK")
		return
	}

	if isIndie(req.URL) {
		serveReverseProxy("http://" + IndependentHost, res, req)
		return
	}

	serveReverseProxy("http://" + CcdHost, res, req)
}

var CcdHost string
var IndependentHost string

func start(bindTo string, ccdHost string, indieHost string) {
	CcdHost = ccdHost
	IndependentHost = indieHost
	http.HandleFunc("/", handleRequestAndRedirect)
	if err := http.ListenAndServe(bindTo, nil); err != nil {
		panic(err)
	}
}

func main() {
	start(os.Getenv("BIND_ADDRESS"),
		os.Getenv("CCD_HOST"),
		os.Getenv("INDIE_HOST"))
}
