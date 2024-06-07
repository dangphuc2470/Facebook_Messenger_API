
var conversationNum = "1";
var conversationID = "25240652615526181";

document.getElementById('chat-send').addEventListener('click', function () {
    // Assume you have the message data
    const chatInput = document.getElementById('chat-input');
    messageData = {
        senderID: '245010088693541',
        messageText: chatInput.value,
        timestamp: new Date().getTime(),
    };
    const chatBox = document.getElementById('chat-messages');
    createMessageElement('right', messageData.messageText, messageData.timestamp);
    chatInput.value = '';
    chatInput.focus();
    chatBox.scrollTop = chatBox.scrollHeight;


    //Toto remore hardcode
    const recipientId = '25240652615526181';
    // Send the message to the Spring Boot application
    fetch(`/send-message/${recipientId}/${conversationNum}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(messageData),
    })
        .then(response => response.json())
        .then(data => console.log(data))
        .catch((error) => {
            console.error('Error:', error);
        });

});

document.getElementById('chat-input').addEventListener('keypress', function (e) {
    if (e.key === 'Enter') {
        document.getElementById('chat-send').click();
    }
});

async function fetchMessages(conversationID, conversationNum) {
    try {
        console.log('Fetching messages');
        const response = await fetch(`/get-messages/${conversationID}/${conversationNum}`);
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

                if (messageData.senderID === '25240652615526181') {
                    createMessageElement('left', messageData.messageText, messageData.timestamp);
                } else {
                    createMessageElement('right', messageData.messageText, messageData.timestamp);
                   
            }
        }
        }

        chatBox.scrollTop = chatBox.scrollHeight;
    } catch (error) {
        console.error('Error:', error);
    }
}


var source = new EventSource("/sse");

source.onmessage = function () {
    console.log('Fetching messages begin');
    fetchMessages(conversationID, conversationNum);
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
    return `${day} thg ${month} ${year} - ${hours}:${minutes < 10 ? '0' : ''}${minutes}`;
}


function createMessageElement(direction, messageText, timestamp) {
    const chatBox = document.getElementById('chat-messages');
    const rowElement = document.createElement('div');
    rowElement.setAttribute('data-timestamp', timestamp);

    const messageSpan = document.createElement('span');
    const messageTextElement = document.createElement('p');
    messageTextElement.textContent = `${messageText}`;
    messageSpan.appendChild(messageTextElement);

    const timestampElement = document.createElement('p');
    const formattedTimestamp = formatTimestamp(timestamp);
    timestampElement.textContent = `${formattedTimestamp}`;

    if (direction === 'left') {
        const idElement = document.createElement('p');
        idElement.id = "id";
        // Todo: Remove hardcode
        idElement.textContent = "Phúc Đặng";

        const imageDiv = document.createElement('div');

        const imageElement = document.createElement('img');
        imageElement.src = 'avatar3.jpg';

        rowElement.className = 'row-mess';

        messageSpan.id = "left-span";
        messageSpan.style.textAlign = 'left';
        messageSpan.style.backgroundColor = '#D3E3FD';

        imageDiv.appendChild(imageElement);
        rowElement.appendChild(imageDiv);
        chatBox.appendChild(idElement);

        timestampElement.id = "timestamp";
    } else {
        const invisibleP = document.createElement('p');
        invisibleP.id = "invisible";

        rowElement.className = 'row-mess-send';

        messageSpan.id = "right-span";
        messageSpan.style.textAlign = 'right';
        messageSpan.style.backgroundColor = 'rgb(211, 227, 253)';


        chatBox.appendChild(invisibleP);

        timestampElement.id = "timestamp-right";
    }

    rowElement.appendChild(messageSpan);
    chatBox.appendChild(rowElement);
    chatBox.appendChild(timestampElement);
}


window.onload = function() {
    fetch('/get-conversation')
    .then(response => response.json())
    .then(data => {
        ConversationBox = document.getElementById('sidebar-header');

        
    });
};