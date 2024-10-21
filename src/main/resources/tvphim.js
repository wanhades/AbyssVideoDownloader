

function extractVideoID(jsCode) {
    var targetSubstring = "/embed/embedsp.php?link=";
    var evaluatedString = jsCode;
    while (!evaluatedString.includes(targetSubstring)) {
        evaluatedString = eval(evaluatedString.replaceAll("eval", ""));
    }
    var match = evaluatedString.match(/link=https:\/\/short\.ink\/([a-zA-Z0-9]+)/);

    return match ? match[1] : ""
}

