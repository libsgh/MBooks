var $ = mdui.$;
function setFontSize(size){
	$(".mdui-typo-body").attr("style","font-size: "+size+"px; padding-top: 100px");
}
function restoreMode() {
	var modeA = document.getElementById("changeModeA");
	if (!modeA) return;
	var mode = 2;
	var text = "夜间模式";
	var fontColor = "#2e343f";
	var backgroundColor = "#E6E0D0";
	if (mode == "1") {
		text = "白天模式";
		fontColor = "#008000";
		backgroundColor = "#000000";
	}
	modeA.innerText = text;
	var texts = document.getElementsByClassName("text");
	for (var i = 0; i < texts.length;i++) {
		if (texts[i].nodeName == "DIV") {
			texts[i].style.color = fontColor;
		}
	}
	var b = document.body;
	b.style.backgroundColor = backgroundColor;
}
function orderList(obj){
	var order = $(obj).text();
	var url = window.location.href;
	url = url.split("?")[0]
	if(order.trim() == "正序"){
		url += "?page=1&order=desc"
	}else{
		url += "?page=1&order=asc"
	}
	window.location.href = url;
}