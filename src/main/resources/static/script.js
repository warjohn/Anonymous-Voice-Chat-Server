// Universal function to send data to the server
function sendDataToServer(url, data) {
    return fetch(url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    }).then(response => {
        if (!response.ok) {
            throw new Error(`Failed to send data to ${url}`);
        }
        return response;
    });
}

// Setup form handlers for setting clients and blacklist
function setupFormHandler(formId, endpoint, successMessage) {
    document.getElementById(formId).addEventListener('submit', function (e) {
        e.preventDefault();
        // Extract and trim input values
        const input = document.getElementById(formId.replace('Form', '')).value.split(',').map(ip => ip.trim());
        // Send data to the server
        sendDataToServer(endpoint, input)
            .then(() => console.log(successMessage))
            .catch(error => console.error(error.message));
    });
}

// Initialize form handlers for clients and blacklist
setupFormHandler('clientsForm', '/set-clients', 'Clients set successfully');
setupFormHandler('blacklistForm', '/set-blacklist', 'Blacklist set successfully');

// Toggle audio recording state
const recordingButton = document.getElementById('recordingButton');
let isRecording = false;
let mediaRecorder;
let audioChunks = [];

recordingButton.addEventListener('click', () => {
    if (isRecording) {
        // Stop recording
        mediaRecorder.stop();
        recordingButton.textContent = 'Start Recording';
        isRecording = false;

        // Send recorded audio to the server
        const audioBlob = new Blob(audioChunks, { type: 'audio/webm' });
        sendAudioToServer(audioBlob);
        audioChunks = []; // Clear audio chunks after sending
    } else {
        // Start recording
        navigator.mediaDevices.getUserMedia({ audio: true })
            .then(stream => {
                audioChunks = []; // Clear audio chunks before starting a new recording
                mediaRecorder = new MediaRecorder(stream);

                // Collect audio data every second
                mediaRecorder.ondataavailable = event => {
                    if (event.data.size > 0) { // Add only non-empty chunks
                        audioChunks.push(event.data);
                    }
                };

                // Handle recording stop event
                mediaRecorder.onstop = () => {
                    console.log('Recording stopped. Total chunks:', audioChunks.length);
                };

                // Start recording with a timeslice of 1000ms (1 second)
                mediaRecorder.start(1000);

                recordingButton.textContent = 'Stop Recording';
                isRecording = true;
            })
            .catch(error => {
                console.error('Error accessing microphone:', error);
            });
    }
});

// Function to send recorded audio to the server
function sendAudioToServer(audioBlob) {
    if (audioBlob.size <= 20) {
        console.error('Recorded audio is too small or empty.');
        return;
    }

    const formData = new FormData();
    formData.append('audio', audioBlob);

    fetch('/upload-audio', {
        method: 'POST',
        body: formData
    })
    .then(response => response.text())
    .then(message => {
        console.log(message);
    })
    .catch(error => {
        console.error('Error uploading audio:', error);
    });
}

// Play audio from the server
document.getElementById('playAudio').addEventListener('click', () => {
    fetch('/get-audio', { method: 'GET' })
        .then(response => response.blob())
        .then(blob => {
            const audioUrl = URL.createObjectURL(blob);
            const audioElement = new Audio(audioUrl);
            audioElement.play();
        })
        .catch(error => {
            console.error('Error playing audio:', error);
        });
});

// Load user list
function loadUsers() {
    fetch('/get-users', { method: 'GET' })
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to fetch users');
            }
            return response.json();
        })
        .then(users => {
            const usersList = document.getElementById('usersList');
            usersList.innerHTML = ''; // Clear the list
            users.forEach(user => {
                const button = document.createElement('button');
                button.className = 'user-button';
                button.textContent = user;
                button.addEventListener('click', () => {
                    document.getElementById('clients').value = user; // Insert user data into Recipients field
                });
                usersList.appendChild(button);
            });
        })
        .catch(error => {
            console.error(error);
        });
}

// Load mail list
function loadMail() {
    fetch('/get-mail', { method: 'GET' })
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to fetch mail');
            }
            return response.json();
        })
        .then(mail => {
            const mailList = document.getElementById('mailList');
            mailList.innerHTML = ''; // Clear the list
            mail.forEach(item => {
                const button = document.createElement('button');
                button.className = 'mail-button';
                button.textContent = item;
                // Handle button click
                button.addEventListener('click', () => {
                    if (button.classList.contains('active')) {
                        // If the button is already active, remove active class and clear mail
                        button.classList.remove('active');
                        sendDataToServer('/clear-mail', { mail: item })
                            .then(response => response.text())
                            .then(message => console.log(message))
                            .catch(error => console.error(error));
                    } else {
                        // If the button is not active, make it active and select mail
                        document.querySelectorAll('.mail-button').forEach(btn => btn.classList.remove('active'));
                        button.classList.add('active');
                        sendDataToServer('/select-mail', { mail: item })
                            .then(response => response.text())
                            .then(message => console.log(message))
                            .catch(error => console.error(error));
                    }
                });
                mailList.appendChild(button);
            });
        })
        .catch(error => {
            console.error(error);
        });
}

// Load data on page load
window.addEventListener('load', () => {
    loadUsers();
    loadMail();
});