
let conversationNum = "2";
let conversationID = "25240652615526181";
let currentUserID = "245010088693541";
let clientName = "Phúc Đặng";
let clientPicture = "/assets/avatar3.jpg";
let selectedConversation = document.createElement('div');
let contacts = document.getElementsByClassName('sidebar-contact');
let currentAD = "AD1";
document.getElementById('chat-send').addEventListener('click', function () {
    sendMessage();

});

document.getElementById('chat-input').addEventListener('keypress', function (e) {
    if (e.key === 'Enter') {
        document.getElementById('chat-send').click();
    }
});



document.addEventListener("DOMContentLoaded", function () {
    fetchConversation(true);
});


function fetchConversation(isFirstLoad = false) {
    console.log('Fetching conversation');
    fetch('http://localhost:8080/get-conversation/' + conversationID)
        .then(response => response.json())
        .then(data => {
            const conversationBox = document.getElementById('.sidebar');

            data.forEach(conversation => {
                const ex = document.getElementById(conversationID + "-" + conversation.conversationNum + "-outer");
                // If not exist that element, create new
                if (!ex) {
                    const conversationElement = document.createElement('div');
                    conversationElement.className = 'sidebar-contact';
                    conversationElement.id = conversation.conversationID + "-" + conversation.conversationNum + "-outer";
                    conversationNum = conversation.conversationNum;


                    conversationElement.addEventListener('click', function () {
                        //conversationID = conversation.conversationID;
                        if (conversationNum !== conversation.conversationNum)
                            document.getElementById('chat-messages').innerHTML = '';
                        conversationNum = conversation.conversationNum;
                        console.log("fet" + conversationID + " " + conversationNum);
                        fetchMessages(conversationID, conversationNum);
                        // console.log(conversationID + " " + conversationNum);
                        // // Todo: remove hard
                        // Document.getElementById('top-name').value = clientName;
                        // Document.getElementById('top-image').src = clientPicture;
                        removeSelectedClass(); // Remove selected class from all contacts
                        this.classList.add('sidebar-contact-selected'); // Add selected class to clicked contact
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
                    lastMessageElement.id = conversationID + "-" + conversationNum;
                    lastMessageElement.textContent = conversation.lastMessage;

                    if (conversation.lastSenderID !== currentUserID) {
                        const badgeElement = document.createElement('span');
                        badgeElement.className = 'badge';
                        rowElement.appendChild(badgeElement);
                        lastMessageElement.style.fontWeight = 'bold';
                        lastMessageElement.style.color = 'black';
                    } else {
                        lastMessageElement.style.fontWeight = 'normal';
                        lastMessageElement.style.color = '#888';
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
                   
                    // The first and second elements of the conversation box are the header and the button container
                    conversationBox.insertBefore(conversationElement, conversationBox.children[2]);
                    
                }

                //conversationElement.className = 'sidebar-contact-selected';


                //selectedConversation.className = 'sidebar-contact'; // Reset the class name of the previously selected conversation

                //selectedConversation = conversationElement;
            });
        })
        .then(() => {
            if (isFirstLoad) {
                //console.log("First load");
                contacts[0].click();
            }
        });
}

async function fetchMessages(conversationID, conversationNum) {
    try {
        console.log('Fetching messages');
        const response = await fetch(`http://localhost:8080/get-messages/${conversationID}/${conversationNum}`);
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        const data = await response.json();
        const chatBox = document.getElementById('chat-messages');
        const chatBoxElementCount = chatBox.childElementCount / 3;
        const dataElementCount = Object.keys(data).length;
        //console.log('chatBoxElementCount:', chatBoxElementCount);
        //console.log('dataElementCount:', dataElementCount);
        //updateConversation();
        const lastElement = chatBox.lastElementChild;
        if (lastElement && lastElement.textContent === 'Đang gửi') {
            lastElement.textContent = formatTimestamp(data[Object.keys(data)[dataElementCount - 1]].timestamp);
        }

        if (chatBoxElementCount < dataElementCount) {
            const keys = Object.keys(data);
            for (let i = chatBoxElementCount; i < dataElementCount; i++) {
                const messageData = data[keys[i]];

                if (messageData.senderID !== currentUserID) {
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

function sendMessage() {
    const chatInput = document.getElementById('chat-input');
    let messageData = {
        senderID: currentUserID,
        messageText: chatInput.value,
        conversationID: conversationID,
        timestamp: new Date().getTime(),
    };
    const chatBox = document.getElementById('chat-messages');
    createMessageElement('right', messageData.messageText, messageData.timestamp, true);
    chatInput.value = '';
    chatInput.focus();
    chatBox.scrollTop = chatBox.scrollHeight;


    // Send the message to the Spring Boot application
    console.log('Sending message:', conversationNum);
    fetch(`http://localhost:8080/send-message/${conversationID}/${conversationNum}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(messageData),
    })
        .then(response => response.json())
        .then(responseString => {
            if (responseString.message === 'OK') {
                updateConversation();
                console.log("Fetch conversation");
            }
            else {
                console.log(responseString.message)
            }
        })
        .catch((error) => {
            console.error('Error:', error);
        });
}

function updateConversation() {
    console.log('Updating conversation');

    fetch('http://localhost:8080/get-conversation/' + conversationID)
        .then(response => response.json())
        .then(data => {
            data.forEach(conversation => {
                //console.log(conversation)
                const id = conversationID + "-" + conversation.conversationNum;
                const lastMessageElement = document.getElementById(id);
                const parentElement = lastMessageElement.parentElement;
                const rowElement = parentElement.querySelector('.row');
                if (lastMessageElement && conversation.conversationNum === conversationNum) {
                    if (conversation.lastSenderID !== conversation.
                        conversationID) {
                        lastMessageElement.textContent = "Bạn: " + conversation.lastMessage;
                        lastMessageElement.style.fontWeight = 'normal';
                        lastMessageElement.style.color = '#888';
                        const badgeElement = rowElement.querySelector('.badge');

                        if (badgeElement) {
                            //console.log("remove badge");
                            rowElement.removeChild(badgeElement);
                            let nameElement = rowElement.querySelector('.span-bold');
                            if (nameElement.textContent.endsWith("N/A")) {
                                nameElement.textContent = clientName + " | " + currentAD;
                            }
                        }

                    }
                    else {
                        lastMessageElement.textContent = conversation.lastMessage;
                        lastMessageElement.style.fontWeight = 'bold';
                        lastMessageElement.style.color = 'black';
                        const badgeElement = rowElement.querySelector('.badge');
                        if (!badgeElement) {
                            const badgeElement = document.createElement('span');
                            badgeElement.className = 'badge';
                            rowElement.appendChild(badgeElement);
                        }
                    }
                }//conversationElement.className = 'sidebar-contact-selected';
                //fetchMessages(conversationID, conversationNum);

                //selectedConversation.className = 'sidebar-contact'; // Reset the class name of the previously selected conversation

                //selectedConversation = conversationElement;
            });
        })
        .then(() => {
            fetchMessages(conversationID, conversationNum);

        });
}

function createMessageElement(direction, messageText, timestamp, isCreateDirectlyAfterSend = false) {
    const chatBox = document.getElementById('chat-messages');
    const rowElement = document.createElement('div');
    rowElement.setAttribute('data-timestamp', timestamp);

    const messageSpan = document.createElement('span');
    const messageTextElement = document.createElement('p');
    messageTextElement.textContent = `${messageText}`;
    messageSpan.appendChild(messageTextElement);

    const timestampElement = document.createElement('p');
    const formattedTimestamp = formatTimestamp(timestamp);
    if (isCreateDirectlyAfterSend)
        timestampElement.textContent = 'Đang gửi';
    else
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

function removeSelectedClass() {
    for (let i = 0; i < contacts.length; i++) {
        contacts[i].classList.remove('sidebar-contact-selected');
    }
}

var ws = new WebSocket('ws://localhost:8081');
ws.onmessage = function (event) {
    console.log('Message from server:', event.data);
    if (event.data === "Reload conversations command sent from Kafka topic")
        fetchConversation(false);
    else
        updateConversation();
};

// document.getElementById('test').addEventListener('click', function () {
//     console.log("test click");
//     updateConversation();
// });