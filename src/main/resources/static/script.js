// Установка получателей
document.getElementById('clientsForm').addEventListener('submit', function(e) {
    e.preventDefault();
    const clients = document.getElementById('clients').value.split(',').map(ip => ip.trim());
    fetch('/set-clients', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(clients)
    }).then(response => {
        if (response.ok) {
            console.log('Clients set successfully');
        } else {
            console.error('Failed to set clients');
        }
    });
});

// Установка черного списка
document.getElementById('blacklistForm').addEventListener('submit', function(e) {
    e.preventDefault();
    const blacklist = document.getElementById('blacklist').value.split(',').map(ip => ip.trim());
    fetch('/set-blacklist', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(blacklist)
    }).then(response => {
        if (response.ok) {
            console.log('Blacklist set successfully');
        } else {
            console.error('Failed to set blacklist');
        }
    });
});

// Переключение состояния записи аудио
const recordingButton = document.getElementById('recordingButton');
let isRecording = false;
recordingButton.addEventListener('click', () => {
    if (isRecording) {
        fetch('/stop-recording', {
            method: 'POST'
        }).then(response => {
            if (response.ok) {
                return response.text();
            } else {
                throw new Error('Failed to stop recording');
            }
        }).then(message => {
            console.log(message);
            recordingButton.textContent = 'Start Recording';
            isRecording = false;
        }).catch(error => {
            console.error(error);
        });
    } else {
        fetch('/start-recording', {
            method: 'POST'
        }).then(response => {
            if (response.ok) {
                return response.text();
            } else {
                throw new Error('Failed to start recording');
            }
        }).then(message => {
            console.log(message);
            recordingButton.textContent = 'Stop Recording';
            isRecording = true;
        }).catch(error => {
            console.error(error);
        });
    }
});

// Воспроизведение аудио
let isPlaying = false;
document.getElementById('playAudio').addEventListener('click', () => {
    const button = document.getElementById('playAudio');
    if (isPlaying) {
        fetch('/stop-audio', {
            method: 'POST'
        }).then(response => {
            if (response.ok) {
                return response.text();
            } else {
                throw new Error('Failed to stop audio');
            }
        }).then(message => {
            console.log(message);
            button.textContent = 'Play Audio';
            isPlaying = false;
        }).catch(error => {
            console.error(error);
        });
    } else {
        fetch('/play-audio', {
            method: 'POST'
        }).then(response => {
            if (response.ok) {
                return response.text();
            } else {
                throw new Error('Failed to play audio');
            }
        }).then(message => {
            console.log(message);
            button.textContent = 'Stop Audio';
            isPlaying = true;
        }).catch(error => {
            console.error(error);
        });
    }
});

// Загрузка списка пользователей
function loadUsers() {
    fetch('/get-users', {
        method: 'GET'
    }).then(response => {
        if (!response.ok) {
            throw new Error('Failed to fetch users');
        }
        return response.json();
    }).then(users => {
        const usersList = document.getElementById('usersList');
        usersList.innerHTML = ''; // Очищаем список
        users.forEach(user => {
            const button = document.createElement('button');
            button.className = 'user-button';
            button.textContent = user;
            button.addEventListener('click', () => {
                const clientsInput = document.getElementById('clients');
                clientsInput.value = user; // Вставляем данные пользователя в поле Recipients
            });
            usersList.appendChild(button);
        });
    }).catch(error => {
        console.error(error);
    });
}

// Загрузка списка почты
function loadMail() {
    fetch('/get-mail', {
        method: 'GET'
    }).then(response => {
        if (!response.ok) {
            throw new Error('Failed to fetch mail');
        }
        return response.json();
    }).then(mail => {
        const mailList = document.getElementById('mailList');
        mailList.innerHTML = ''; // Очищаем список
        mail.forEach(item => {
            const button = document.createElement('button');
            button.className = 'mail-button';
            button.textContent = item;
            // Обработчик нажатия на кнопку
            button.addEventListener('click', () => {
                if (button.classList.contains('active')) {
                    // Если кнопка уже активна, убираем активный класс и отправляем "clear"
                    button.classList.remove('active');
                    fetch('/clear-mail', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ mail: item })
                    }).then(response => {
                        if (response.ok) {
                            return response.text();
                        } else {
                            throw new Error('Failed to clear mail');
                        }
                    }).then(message => {
                        console.log(message);
                    }).catch(error => {
                        console.error(error);
                    });
                } else {
                    // Если кнопка не активна, делаем её активной и отправляем "select"
                    document.querySelectorAll('.mail-button').forEach(btn => btn.classList.remove('active'));
                    button.classList.add('active');
                    fetch('/select-mail', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ mail: item })
                    }).then(response => {
                        if (response.ok) {
                            return response.text();
                        } else {
                            throw new Error('Failed to select mail');
                        }
                    }).then(message => {
                        console.log(message);
                    }).catch(error => {
                        console.error(error);
                    });
                }
            });
            mailList.appendChild(button);
        });
    }).catch(error => {
        console.error(error);
    });
}

// Загружаем данные при загрузке страницы
window.addEventListener('load', () => {
    loadUsers();
    loadMail();
});