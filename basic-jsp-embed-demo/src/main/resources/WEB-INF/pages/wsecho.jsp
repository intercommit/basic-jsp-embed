<%@ include file="/includes/header.jsp" %> 
<body>
<!-- Copied from http://svn.apache.org/viewvc/tomcat/trunk/webapps/examples/websocket/echo.xhtml?view=log -->
<script src="${domainRoot}/static/wsecho.js"></script>
<div class="noscript"><h2 style="color: #ff0000">Seems your browser doesn't support Javascript! Websockets rely on Javascript being enabled. Please enable
    Javascript and reload this page!</h2></div>
Example code copied from Tomcat's echo <a href="http://svn.apache.org/viewvc/tomcat/trunk/webapps/examples/websocket/">websocket examples</a>
<br>
<div>
    <div id="connect-container">
        <div>
            <span>Connect to service implemented using:</span>
            <br/>
            <input id="radio4" type="radio" name="group1" value="${domainRoot}/websocket/echoAsyncAnnotation"
                   onclick="updateTarget(this.value);"/> <label for="radio4">annotation API (async)</label>
        </div>
        <div>
            <input id="target" type="text" size="40" style="width: 350px"/>
        </div>
        <div>
            <button id="connect" onclick="connect();">Connect</button>
            <button id="disconnect" disabled="disabled" onclick="disconnect();">Disconnect</button>
        </div>
        <div>
            <textarea id="message" style="width: 350px">Here is a message!</textarea>
        </div>
        <div>
            <button id="echo" onclick="echo();" disabled="disabled">Echo message</button>
        </div>
    </div>
    <div id="console-container">
       	<div id="console"></div>
    </div>
</div>
</body>
</html>