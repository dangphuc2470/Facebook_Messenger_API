document.getElementById('chat-send').addEventListener('click', function () {
    // Assume you have the message data
    const chatInput = document.getElementById('chat-input');
    messageData = {
        senderID: '25240652615526181',
        messageText: chatInput.value,
        timestamp: new Date().getTime()
    };
    console.log('Send:', messageData.messageText);

    const chatBox = document.getElementById('chat-messages');

    // Create a new div element with class "row-mess-send"
    const rowElement = document.createElement('div');
    rowElement.className = 'row-mess-send';
    rowElement.setAttribute('data-timestamp', messageData.timestamp);

    // Create a new span for the message text
    const messageSpan = document.createElement('span');
    messageSpan.id = "right-span";
    messageSpan.style.textAlign = 'right';
    messageSpan.style.backgroundColor = 'rgb(211, 227, 253)';

    // Create a new p element for the message text and append it to messageSpan
    const messageTextElement = document.createElement('p');
    messageTextElement.textContent = messageData.messageText;
    messageSpan.appendChild(messageTextElement);

    // Append messageSpan to the rowElement
    rowElement.appendChild(messageSpan);

    // Append the rowElement to the chatBox
    chatBox.appendChild(rowElement);

    // Create a new p element for the timestamp
    const timestampElement = document.createElement('p');
    timestampElement.id = "timestamp-right";
    const formattedTimestamp = formatTimestamp(messageData.timestamp);
    timestampElement.textContent = `${formattedTimestamp}`;

    // Append the timestampElement to the chatBox
    chatBox.appendChild(timestampElement);

    chatInput.value = '';
    chatInput.focus();
    chatBox.scrollTop = chatBox.scrollHeight;

    // var chatInput = document.getElementById('chat-input');
    // var chatMessages = document.getElementById('chat-messages');

    // if (chatInput.value.trim() !== '') {
    //     var newMessage = document.createElement('p');
    //     newMessage.textContent = "Send: " + chatInput.value;
    //     chatMessages.appendChild(newMessage);

    //     // Send the message to the Spring Boot application
    //     fetch('/send-message', {
    //         method: 'POST',
    //         headers: {
    //             'Content-Type': 'application/json',
    //         },
    //         body: JSON.stringify({
    //             message: chatInput.value
    //         }),
    //     })
    //         .then(response => response.json())
    //         .then(data => {
    //             console.log('Success:', data);
    //         })
    //         .catch((error) => {
    //             console.error('Error:', error);
    //         });

    //     // Clear the input field and refocus it for the next message
    //     chatInput.value = '';
    //     chatInput.focus();
    // }
});

document.getElementById('chat-input').addEventListener('keypress', function (e) {
    if (e.key === 'Enter') {
        document.getElementById('chat-send').click();
    }
});

function SendMessage(message) {
    console.log('Send:', message);
    var url = 'https://graph.facebook.com/v18.0/me/messages?access_token=EABsLirhuG9kBO1kHgWn5AecLhNPksgVKogL72F4oB8sCZB9roIZC02Uxv4IngGG0SZCJzseTeBwaJSyKK43ZAkZC5oR3Tg3iu3VSJGxl1c3VhFAFbIrBzWi1Cqt4gljsbIPJpxyXJsXKGw1QIVNgunF2d755bOXyqQ9FjZA17dyb5yUZC1eusvn7RL2orzrbT4ZD';

    var data = {
        "message": {
            "text": message
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

//
// // Hàm này sẽ được gọi để lấy dữ liệu mới từ server
// function fetchLatestMessage() {
//     fetch('https://firmly-engaged-duckling.ngrok-free.app/latest-message')
//         .then(response => response.text())
//         .then(data => {
//             if (data.startsWith('<!doctype')) {
//                 console.log(data);
//                 return;
//                 //console.log(data.match(/<noscript>(.*?)<\/noscript>/));
//             }
//             else if (!data) {
//                 console.log('No data received from server');
//                 return;
//             }
//             var jsonData = JSON.parse(data);
//             var messageText = jsonData.entry[0].messaging[0].message.text;
//             console.log(messageText); // This will log "hi"
//
//             var chatBox = document.getElementById('chat-messages');
//             var messageElement = document.createElement('p');
//             messageElement.textContent = "Receive: " + messageText;
//             chatBox.appendChild(messageElement);
//             chatBox.scrollTop = chatBox.scrollHeight;
//         });
// }
//
// // Gọi hàm fetchLatestMessage mỗi 1 giây (1000 mili giây)
// setInterval(fetchLatestMessage, 1000);

async function fetchMessages() {
    try {
        console.log('Fetching messages');
        const response = await fetch('/get-messages'); // Replace with the actual endpoint to get messages
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        const data = await response.json();
        const chatBox = document.getElementById('chat-messages');
        const chatBoxElementCount = chatBox.childElementCount / 3;
        const dataElementCount = Object.keys(data).length;
        console.log('chatBoxElementCount:', chatBoxElementCount);
        console.log('dataElementCount:', dataElementCount);


        if (chatBoxElementCount < dataElementCount) {
            const keys = Object.keys(data);
            for (let i = chatBoxElementCount; i < dataElementCount; i++) {
                const messageData = data[keys[i]];

                // Create a new p element for the sender's ID
                const idElement = document.createElement('p');
                idElement.id = "id";
                // Todo: Remove hardcode
                idElement.textContent = "Phúc Đặng";

                // Append the idElement to the chatBox
                chatBox.appendChild(idElement);

                // Create a new div element with class "row-mess"
                const rowElement = document.createElement('div');
                rowElement.className = 'row-mess';
                // Convert the timestamp to a human-readable format

                rowElement.setAttribute('data-timestamp', messageData.timestamp);

                // Create a new div for the image
                const imageDiv = document.createElement('div');

                // Create a new img element and append it to imageDiv
                const imageElement = document.createElement('img');
                imageElement.src = 'avatar3.jpg'; // Replace with the actual path to your image
                imageElement.alt = 'Image description'; // Replace with a suitable alt text
                imageDiv.appendChild(imageElement);

                // Append imageDiv to the rowElement
                rowElement.appendChild(imageDiv);

                // Create a new span for the message text
                const messageSpan = document.createElement('span');
                messageSpan.id = "left-span";

                // Create a new p element for the message text and append it to messageSpan
                const messageTextElement = document.createElement('p');
                messageTextElement.textContent = `${messageData.messageText}`;
                messageSpan.appendChild(messageTextElement);

                // Todo: Remove hardcode
                if (messageData.senderID === '25240652615526181') {
                    messageSpan.style.textAlign = 'right';
                    messageSpan.style.backgroundColor = '#D3E3FD';
                } else {
                    messageSpan.style.textAlign = 'left';
                    messageSpan.style.backgroundColor = '#FAD8FD';
                }

                // Append messageSpan to the rowElement
                rowElement.appendChild(messageSpan);

                // Append the rowElement to the chatBox
                chatBox.appendChild(rowElement);

                // Create a new p element for the timestamp
                const timestampElement = document.createElement('p');
                timestampElement.id = "timestamp";

                const formattedTimestamp = formatTimestamp(messageData.timestamp);

                timestampElement.textContent = `${formattedTimestamp}`;

                // Append the timestampElement to the chatBox
                chatBox.appendChild(timestampElement);
            }
        }

        chatBox.scrollTop = chatBox.scrollHeight;
    } catch (error) {
        console.error('Error:', error);
    }
}


var source = new EventSource("/sse");

source.onmessage = function (event) {
    console.log('Fetching messages begin');
    fetchMessages();
};


function formatTimestamp(timestamp) {
    const date = new Date(timestamp);

    // Extract the date, month, year, and time
    const day = date.getDate();
    const month = date.getMonth() + 1; // getMonth() returns a zero-based month
    const year = date.getFullYear();
    const hours = date.getHours();
    const minutes = date.getMinutes();

    // Format the timestamp
    const formattedTimestamp = `${day} thg ${month} ${year} - ${hours}:${minutes < 10 ? '0' : ''}${minutes}`;

    return formattedTimestamp;
}
