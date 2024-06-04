document.getElementById('chat-send').addEventListener('click', function() {
    var chatInput = document.getElementById('chat-input');
    var chatMessages = document.getElementById('chat-messages');

    if(chatInput.value.trim() !== '') {
        var newMessage = document.createElement('p');
        newMessage.textContent = "Send: " + chatInput.value;
        chatMessages.appendChild(newMessage);

        // Send the message to the Spring Boot application
        fetch('/send-message', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                message: chatInput.value
            }),
        })
        .then(response => response.json())
        .then(data => {
            console.log('Success:', data);
        })
        .catch((error) => {
            console.error('Error:', error);
        });

        // Clear the input field and refocus it for the next message
        chatInput.value = '';
        chatInput.focus();
    }
});

document.getElementById('chat-input').addEventListener('keypress', function(e) {
    if (e.key === 'Enter') {
        document.getElementById('chat-send').click();
    }
});

function SendMessage(message) {
    console.log('Send:', message);
    var url = 'https://graph.facebook.com/v18.0/me/messages?access_token=EABsLirhuG9kBO1kHgWn5AecLhNPksgVKogL72F4oB8sCZB9roIZC02Uxv4IngGG0SZCJzseTeBwaJSyKK43ZAkZC5oR3Tg3iu3VSJGxl1c3VhFAFbIrBzWi1Cqt4gljsbIPJpxyXJsXKGw1QIVNgunF2d755bOXyqQ9FjZA17dyb5yUZC1eusvn7RL2orzrbT4ZD';

    var data = {
        "message": {
            "text":message
        },
        "messaging_type": "RESPONSE",
        "recipient": {
            "id": "25240652615526181"
        }
    };

    fetch(url, {
        method: 'POST', 
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(data), 
    })
    .then(response => response.json())
    .then(data => {
        console.log('Success:', data);
    })
    .catch((error) => {
        console.error('Error:', error);
    });
}

// Hàm này sẽ được gọi để lấy dữ liệu mới từ server
function fetchLatestMessage() {
    fetch('https://firmly-engaged-duckling.ngrok-free.app/latest-message')
        .then(response => response.text())
        .then(data => {
            if (data.startsWith('<!doctype')) {
                console.log(data);
                return;
                //console.log(data.match(/<noscript>(.*?)<\/noscript>/));
            }
            else if (!data) {
                console.log('No data received from server');
                return;
            }
            var jsonData = JSON.parse(data);
            var messageText = jsonData.entry[0].messaging[0].message.text;
            console.log(messageText); // This will log "hi"

            var chatBox = document.getElementById('chat-messages');
            var messageElement = document.createElement('p');
            messageElement.textContent = "Receive: " + messageText;
            chatBox.appendChild(messageElement);
            chatBox.scrollTop = chatBox.scrollHeight;
        });
}

// Gọi hàm fetchLatestMessage mỗi 1 giây (1000 mili giây)
setInterval(fetchLatestMessage, 1000);