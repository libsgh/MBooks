var $ = mdui.$;
function setFontSize(size){
	$(".mdui-typo-body").attr("style","font-size: "+size+"px; padding-top: 5px");
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
	var texts = document.getElementsByClassName("mdui-typo-body");
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
function timeago(dateTimeStamp){
	dateTimeStamp = dateTimeStamp*1000;
    var minute = 1000 * 60;
    var hour = minute * 60;
    var day = hour * 24;
    var week = day * 7;
    var halfamonth = day * 15;
    var month = day * 30;
    var now = new Date().getTime();
    var diffValue = now - dateTimeStamp;
    if(diffValue < 0){
        return "";
    }
    var minC = diffValue/minute;
    var hourC = diffValue/hour;
    var dayC = diffValue/day;
    var weekC = diffValue/week;
    var monthC = diffValue/month;
    if(monthC >= 1 && monthC <= 3){
        result = " " + parseInt(monthC) + "月前";
    }else if(weekC >= 1 && weekC <= 3){
        result = " " + parseInt(weekC) + "周前";
    }else if(dayC >= 1 && dayC <= 6){
        result = " " + parseInt(dayC) + "天前";
    }else if(hourC >= 1 && hourC <= 23){
        result = " " + parseInt(hourC) + "小时前";
    }else if(minC >= 1 && minC <= 59){
        result =" " + parseInt(minC) + "分钟前";
    }else if(diffValue >= 0 && diffValue <= minute){
        result = "刚刚";
    }else {
        var datetime = new Date();
        datetime.setTime(dateTimeStamp);
        var Nyear = datetime.getFullYear();
        var Nmonth = datetime.getMonth() + 1 < 10 ? "0" + (datetime.getMonth() + 1) : datetime.getMonth() + 1;
        var Ndate = datetime.getDate() < 10 ? "0" + datetime.getDate() : datetime.getDate();
        var Nhour = datetime.getHours() < 10 ? "0" + datetime.getHours() : datetime.getHours();
        var Nminute = datetime.getMinutes() < 10 ? "0" + datetime.getMinutes() : datetime.getMinutes();
        var Nsecond = datetime.getSeconds() < 10 ? "0" + datetime.getSeconds() : datetime.getSeconds();
        result = Nyear + "-" + Nmonth + "-" + Ndate;
    }
    return result;
}
//读取看书历史
function readHistory(bookId, last){
	var data = Cookies.get(bookId);
	if(data){
		var d = JSON.parse(data);
		if(last-d.index != 0){
			$("#read-index-"+bookId).find("span").text(last-d.index);
			$("#read-index-"+bookId).show();
			$(".readed").show();
			$("#read-cname-"+bookId).html("您已读至："+d.cname);
			$("#read-cname-"+bookId).show();
			$("#timeF-"+bookId).hide();
		}else{
			$("#timeF-"+bookId).show();
		}
	}
}
//记录读书进度
function recordHistory(bookId, index, cname, cId){
	var info = {};
	info["bookId"] = bookId;
	info["index"] = index;
	info["cname"] = cname;
	info["cId"] = cId;
	Cookies.set(bookId, JSON.stringify(info), { expires: 365 });
}
function startRead(bookId){
	var data = Cookies.get(bookId);
	if(data){
		var d = JSON.parse(data);
		$("#startReadBtn").text("继续阅读");
		//$("#startReadBtn").attr("onclick", "location.href='/c/next/"+d.index+"?bId="+d.bookId+"'");
		$("#startReadBtn").attr("onclick", "location.href='/c/"+d.cId+"'");
	}
}