
var conversationNum = "2";
var conversationID = "25240652615526181";
var currentUserID = "245010088693541";
var clientName = "Phúc Đặng";
var clientPicture = "avatar3.jpg";
var selectedConversation = document.createElement('div');
var contacts = document.getElementsByClassName('sidebar-contact');

// Function to remove selected class from all contacts
function removeSelectedClass() {
    for (var i = 0; i < contacts.length; i++) {
        contacts[i].classList.remove('sidebar-contact-selected');
    }
}

document.getElementById('chat-send').addEventListener('click', function () {
    const chatInput = document.getElementById('chat-input');
    messageData = {
        senderID: currentUserID,
        messageText: chatInput.value,
        timestamp: new Date().getTime(),
    };
    const chatBox = document.getElementById('chat-messages');
    createMessageElement('right', messageData.messageText, messageData.timestamp);
    chatInput.value = '';
    chatInput.focus();
    chatBox.scrollTop = chatBox.scrollHeight;


    // Send the message to the Spring Boot application
    fetch(`/send-message/${conversationID}/${conversationNum}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(messageData),
    })
        .then(response => response.json())
        .then(responseString => {
            console.log(responseString);

            if (responseString.message === 'Message sent successfully') {


            }
        })
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

                if (messageData.senderID != currentUserID) {
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
        idElement.textContent = clientName;

        const imageDiv = document.createElement('div');

        const imageElement = document.createElement('img');
        imageElement.src = clientPicture;

        rowElement.className = 'row-mess';

        messageSpan.id = "left-span";
        imageDiv.appendChild(imageElement);
        rowElement.appendChild(imageDiv);
        chatBox.appendChild(idElement);

        timestampElement.id = "timestamp";
    } else {
        const invisibleP = document.createElement('p');
        invisibleP.id = "invisible";

        rowElement.className = 'row-mess-send';

        messageSpan.id = "right-span";

        chatBox.appendChild(invisibleP);

        timestampElement.id = "timestamp-right";
    }

    rowElement.appendChild(messageSpan);
    chatBox.appendChild(rowElement);
    chatBox.appendChild(timestampElement);
}

document.addEventListener("DOMContentLoaded", function () {
    fetch('/get-conversation/' + conversationID)
        .then(response => response.json())
        .then(data => {
            const conversationBox = document.getElementById('.sidebar');




            data.forEach(conversation => {
                const conversationElement = document.createElement('div');
                conversationElement.className = 'sidebar-contact';

                conversationElement.addEventListener('click', function () {
                    //conversationID = conversation.conversationID;
                    conversationNum = conversation.conversationNum;

                    // // Todo: remove hard
                    // Document.getElementById('top-name').value = clientName;
                    // Document.getElementById('top-image').src = clientPicture;
                    fetchMessages(conversationID, conversationNum);
                    removeSelectedClass(); // Remove selected class from all contacts
                    this.classList.add('sidebar-contact-selected'); // Add selected class to clicked contact
                    document.getElementById('chat-messages').innerHTML = '';
                    fetchMessages(conversationID, conversationNum);
                }
                );



                const imgElement = document.createElement('img');
                imgElement.src = clientPicture;
                imgElement.alt = 'avatar';
                conversationElement.appendChild(imgElement);

                const infoElement = document.createElement('div');
                infoElement.className = 'contact-info';
                conversationElement.appendChild(infoElement);

                const rowElement = document.createElement('div');
                rowElement.className = 'row';
                infoElement.appendChild(rowElement);

                const nameElement = document.createElement('span');
                nameElement.className = 'span-bold';
                nameElement.textContent = clientName + " | " + (conversation.advisorId || 'N/A');
                rowElement.appendChild(nameElement);

                const lastMessageElement = document.createElement('span');
                lastMessageElement.className = 'last-message';
                lastMessageElement.textContent = conversation.lastMessage;

                if (conversation.lastSenderID != currentUserID) {
                    const badgeElement = document.createElement('span');
                    badgeElement.className = 'badge';
                    rowElement.appendChild(badgeElement);
                    lastMessageElement.style.fontWeight = 'bold';
                    lastMessageElement.style.color = 'black';
                }
                else {
                    lastMessageElement.textContent = "Bạn: " + lastMessageElement.textContent;
                }

                infoElement.appendChild(lastMessageElement);


                const timestampElement = document.createElement('span');
                timestampElement.className = 'timestamp';
                timestampElement.textContent = formatTimestamp(conversation.lastMessageTimestamp);
                infoElement.appendChild(timestampElement);

                const badgeRightElement = document.createElement('span');
                badgeRightElement.className = 'badge-right';
                badgeRightElement.textContent = conversation.conversationNum;
                conversationElement.appendChild(badgeRightElement);
                conversationBox.appendChild(conversationElement);

                //conversationElement.className = 'sidebar-contact-selected';


                //selectedConversation.className = 'sidebar-contact'; // Reset the class name of the previously selected conversation

                //selectedConversation = conversationElement;
            });
        })
        .then(() => {
            contacts[0].click();
        });
});


