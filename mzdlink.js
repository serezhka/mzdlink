(function() {

  function startMzdlink() {
    $(document).ready(function() {

      var ws = new ReconnectingWebSocket("ws://127.0.0.1:8080/mzdlink");

      var MOBILE_SCREEN = {
        x: 1920,
        y: 1080
      };

      var MAZDA_SCREEN = {
        x: 800,
        y: 480
      };

      var PORTRAIT_MODE = {
        width: Math.floor(MAZDA_SCREEN.y * (MAZDA_SCREEN.y / MAZDA_SCREEN.x)),
        x: Math.floor((MAZDA_SCREEN.x - MAZDA_SCREEN.y * (MAZDA_SCREEN.y / MAZDA_SCREEN.x)) / 2)
      }

      var BLANK_IMG = 'data:image/gif;base64,R0lGODlhAQABAAAAACH5BAEKAAEALAAAAAABAAEAAAICTAEAOw==';

      var canvas = document.createElement("canvas");
      canvas.id = "mobile-screen";
      canvas.width = MAZDA_SCREEN.x;
      canvas.height = MAZDA_SCREEN.y;
      canvas.style.left = 0;
      canvas.style.top = 0;
      canvas.style.zIndex = 1010;
      canvas.style.position = "absolute";
      canvas.style.border = "1px solid";
      document.body.appendChild(canvas);

      var g = canvas.getContext('2d');
      g.fillStyle = "#FF0000";
      g.font = 'italic small-caps bold 15px arial';

      var landscape = false;

      ws.onmessage = function(evt) {
        var img = new Image();
        img.onload = function() {
          if (landscape = img.width > img.height) {
            g.drawImage(img, 0, 0, canvas.width, canvas.height);
          } else {
            g.clearRect(0, 0, canvas.width, canvas.height);
            g.drawImage(img, PORTRAIT_MODE.x, 0, PORTRAIT_MODE.width, canvas.height);
          }
          img.onload = null;
          img.src = BLANK_IMG;
          img = null;
        }
        img.src = "data:image/png;base64," + evt.data;
      };

      // Touch hook
      var hammer = new Hammer(canvas);
      hammer.get('pan').set({
        direction: Hammer.DIRECTION_ALL
      });
      hammer.on("hammer.input panmove", function(evt) {
        var x, origX = evt.pointers[0].layerX;
        var y, origY = evt.pointers[0].layerY;
        g.clearRect(0, 0, 100, 100);
        g.fillText(origX + " : " + origY, 10, 20);
        if (ws.readyState !== WebSocket.OPEN) return;
        if (landscape) {
          x = Math.floor(MOBILE_SCREEN.x * origX / MAZDA_SCREEN.x);
          y = MOBILE_SCREEN.y - Math.floor(MOBILE_SCREEN.y * origY / MAZDA_SCREEN.y);
        } else {
          if (origX < PORTRAIT_MODE.x || origX > PORTRAIT_MODE.x + PORTRAIT_MODE.width) {
            ws.send("u 0\nc\n");
            return;
          }
          y = Math.floor(MOBILE_SCREEN.y * (origX - PORTRAIT_MODE.x) / PORTRAIT_MODE.width);
          x = Math.floor(MOBILE_SCREEN.x * origY / MAZDA_SCREEN.y);
        }
        switch (evt.type) {
          case "hammer.input":
            if (evt.isFirst) {
              ws.send("d 0 " + y + " " + x + "\nc\n");
            } else if (evt.isFinal) {
              ws.send("u 0\nc\n");
            }
            break;
          case "panmove":
            ws.send("m 0 " + y + " " + x + "\nc\n");
            break;
        }
      });
    });
  }

  document.addEventListener('DOMContentLoaded', function() {
    if (!document.getElementById("jquery-script")) {
      var docBody = document.getElementsByTagName("body")[0];
      if (docBody) {
        var script = document.createElement("script");
        script.setAttribute("id", "jquery-script");
        script.setAttribute("src", "jquery-3.1.1.min.js"); // https://code.jquery.com/jquery-3.1.1.min.js 
        script.addEventListener('load', function() {
          var script = document.createElement("script");
          script.setAttribute("id", "hammer-script");
          script.setAttribute("src", "hammer.min.js"); // http://hammerjs.github.io/dist/hammer.min.js
          script.addEventListener('load', function() {
            var script = document.createElement("script");
            script.setAttribute("id", "websocket-script");
            script.setAttribute("src", "reconnecting-websocket.min.js"); // https://rawgit.com/joewalnes/reconnecting-websocket/master/reconnecting-websocket.min.js
            script.addEventListener('load', function() {
              var script = document.createElement("script");
              script.textContent = "(" + startMzdlink.toString() + ")();";
              docBody.appendChild(script);
            }, false);
            docBody.appendChild(script);
          }, false);
          docBody.appendChild(script);
        }, false);
        docBody.appendChild(script);
      }
    }
  });
})();
