var jolokia = new Jolokia(jolokiaAgentUrl);
var response = jolokia.request({type: "exec", mbean: "jolokia:type=ServerHandler,qualifier=jspdemo", operation: "mBeanServersInfo"}, {method: "post"});
var container = document.getElementById("jmxinfo");
var txt = document.createTextNode(response.value); 
container.appendChild(txt);
