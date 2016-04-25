"use strict";

var ws = null;

function setConnected(connected) {
	document.getElementById('connect').disabled = connected;
	document.getElementById('disconnect').disabled = !connected;
	document.getElementById('echo').disabled = !connected;
}

function connect() {
	var target = document.getElementById('target').value;
	if (target == '') {
		alert('Please select server side connection implementation.');
		return;
	}
	if ('WebSocket' in window) {
		ws = new WebSocket(target);
	} else if ('MozWebSocket' in window) {
		ws = new MozWebSocket(target);
	} else {
		alert('WebSocket is not supported by this browser.');
		return;
	}
	ws.onopen = function () {
		setConnected(true);
		log('Info: WebSocket connection opened.');
	};
	ws.onmessage = function (event) {
		log('Received: ' + event.data);
	};
	ws.onclose = function (event) {
		setConnected(false);
		log('Info: WebSocket connection closed, Code: ' + event.code + (event.reason == "" ? "" : ", Reason: " + event.reason));
	};
}

function disconnect() {
	if (ws != null) {
		ws.close();
		ws = null;
	}
	setConnected(false);
}

function echo() {
	if (ws != null) {
		var message = document.getElementById('message').value;
		log('Sent: ' + message);
		ws.send(message);
	} else {
		alert('WebSocket connection not established, please connect.');
	}
}

function updateTarget(target) {
	if (window.location.protocol == 'http:') {
		document.getElementById('target').value = 'ws://' + window.location.host + target;
	} else {
		document.getElementById('target').value = 'wss://' + window.location.host + target;
	}
}

HTMLElement.prototype.insertFirst = function(childNode) {
    if (this.firstChild) {
		this.insertBefore(childNode, this.firstChild);
	} else {
		this.appendChild(childNode);
	}
}

function log(message) {
	var console = document.getElementById('console');
	console.insertFirst(document.createElement('br'));
	var p = document.createElement('code');
	p.appendChild(document.createTextNode(message));
	console.insertFirst(p);
	while (console.childNodes.length > 25) {
		console.removeChild(console.lastChild);
	}
	console.scrollTop = console.scrollHeight;
}

document.addEventListener("DOMContentLoaded", function() {
	// Remove elements with "noscript" class - <noscript> is not allowed in XHTML
	var noscripts = document.getElementsByClassName("noscript");
	for (var i = 0; i < noscripts.length; i++) {
		noscripts[i].parentNode.removeChild(noscripts[i]);
	}
}, false);
