// গুগল শিটের ওয়েব অ্যাপ ইউআরএল (পরবর্তী ধাপে আমরা এখানে আপনার লিংক বসাবো)
const GOOGLE_SHEET_SCRIPT_URL = "YOUR_GOOGLE_SHEET_WEB_APP_URL";

let quizData = [];
let timer;
let totalSeconds = 30 * 60; // ডিফল্ট ৩০ মিনিট, শিট থেকে চেঞ্জ করা যাবে
const answeredQuestions = new Set();

// অ্যাপ লোড হওয়ার সাথে সাথে গুগল শিট থেকে প্রশ্ন নিয়ে আসবে
window.addEventListener('DOMContentLoaded', () => {
    fetchQuestionsFromSheet();
});

function fetchQuestionsFromSheet() {
    const container = document.getElementById('exam-screen');
    container.innerHTML = '<div class="card" style="text-align:center;">পরীক্ষার প্রশ্ন লোড হচ্ছে... অনুগ্রহ করে অপেক্ষা করুন।</div>';
    
    // যদি গুগল শিটের লিংক সেট না থাকে, ডেমো ডাটা দেখাবে
    if(GOOGLE_SHEET_SCRIPT_URL === "YOUR_GOOGLE_SHEET_WEB_APP_URL") {
        container.innerHTML = '<div class="card" style="text-align:center; color:#ff6b6b;">দয়া করে ধাপ ৪ সম্পন্ন করে গুগল শিটের লিংকটি app.js এ বসান।</div>';
        return;
    }

    fetch(GOOGLE_SHEET_SCRIPT_URL)
        .then(response => response.json())
        .then(data => {
            quizData = data;
            container.innerHTML = '<div class="card" style="text-align:center; color:#55c57a;">প্রশ্ন সফলভাবে লোড হয়েছে! নাম ও ফোন নম্বর দিয়ে পরীক্ষা শুরু করুন।</div>';
        })
        .catch(error => {
            console.error('Error:', error);
            container.innerHTML = '<div class="card" style="text-align:center; color:#ff6b6b;">প্রশ্ন লোড করতে সমস্যা হয়েছে। লিংকটি আবার চেক করুন।</div>';
        });
}

// স্ক্রিনে ডাইনামিকালি প্রশ্ন সাজানোর ফাংশন (১ থেকে ২০০ যত প্রশ্নই থাক)
function renderQuestions() {
    const container = document.getElementById('exam-screen');
    container.innerHTML = ''; 

    quizData.forEach((data, index) => {
        const block = document.createElement('div');
        block.className = 'card';
        block.style.marginBottom = '20px';
        block.style.maxWidth = '100%';

        let optionsHtml = '';
        const labels = ['ক', 'খ', 'গ', 'ঘ'];
        
        // শিট থেকে আসা ৪টি অপশন
        const optionsArray = [data.optionA, data.optionB, data.optionC, data.optionD];
        
        optionsArray.forEach((opt, oIndex) => {
            optionsHtml += `
                <label class="option-label" style="display: block; padding: 12px; background: #2b2b2b; margin-bottom: 10px; border-radius: 8px; cursor: pointer; border: 1px solid #444;">
                    <input type="radio" name="q${index}" value="${oIndex}" onchange="updateProgress(${index})" style="margin-right: 10px;">
                    ${labels[oIndex]}) ${opt}
                </label>
            `;
        });

        // সঠিক উত্তরের ইনডেক্স নির্ধারণ (ক=০, খ=১, গ=২, ঘ=৩)
        const correctIndex = labels.indexOf(data.correctOption.trim());

        block.innerHTML = `
            <div class="question-title" style="font-size: 18px; font-weight: 600; margin-bottom: 15px; line-height: 1.5;">${index + 1}। ${data.question}</div>
            ${optionsHtml}
            <div class="explanation" id="exp${index}" style="display: none; margin-top: 15px; padding: 15px; background: #1a1a1a; border-left: 4px solid #a8c7fa; font-size: 14px; color: #ccc; line-height: 1.6;">
                <div style="color: #55c57a; font-weight: bold; margin-bottom: 8px;">সঠিক উত্তর → ${data.correctOption}) ${optionsArray[correctIndex]}</div>
                <p><strong>ব্যাখ্যা:</strong> ${data.explanation} <em>(Create by Bornochorcha)</em></p>
            </div>
        `;
        container.appendChild(block);
    });

    // সাবমিট বাটন
    const submitBtn = document.createElement('button');
    submitBtn.className = 'btn-start';
    submitBtn.innerText = 'Submit Exam';
    submitBtn.style.backgroundColor = '#55c57a';
    submitBtn.style.color = '#fff';
    submitBtn.onclick = submitExam;
    container.appendChild(submitBtn);

    // নিচের টোটাল প্রোগ্রেস বার আপডেট করা (১ থেকে ২০০ যত প্রশ্নই হোক)
    document.getElementById('progress-display').innerText = `0/${quizData.length}`;
}

function updateProgress(index) {
    answeredQuestions.add(index);
    document.getElementById('progress-display').innerText = `${answeredQuestions.size}/${quizData.length}`;
}

function startExam() {
    const name = document.getElementById('student-name').value.trim();
    const phone = document.getElementById('student-phone').value.trim();

    if (!name || !phone) {
        alert("দয়া করে নাম এবং ফোন নম্বর দিন!");
        return;
    }

    if(quizData.length === 0) {
        alert("প্রশ্ন এখনো লোড হয়নি, একটু অপেক্ষা করুন!");
        return;
    }

    document.getElementById('login-screen').style.display = 'none';
    document.getElementById('exam-screen').style.display = 'block';

    renderQuestions();
    timer = setInterval(runTimer, 1000);
}

function runTimer() {
    let minutes = Math.floor(totalSeconds / 60);
    let seconds = totalSeconds % 60;

    seconds = seconds < 10 ? '0' + seconds : seconds;
    minutes = minutes < 10 ? '0' + minutes : minutes;

    document.getElementById('timer-display').innerText = `${minutes}:${seconds}`;

    if (totalSeconds <= 0) {
        clearInterval(timer);
        alert("সময় শেষ! পরীক্ষাটি স্বয়ংক্রিয়ভাবে সাবমিট হচ্ছে।");
        submitExam();
    }
    totalSeconds--;
}

function submitExam() {
    clearInterval(timer);
    let score = 0;
    const labels = ['ক', 'খ', 'গ', 'ঘ'];

    quizData.forEach((data, index) => {
        const selected = document.querySelector(`input[name="q${index}"]:checked`);
        const correctIndex = labels.indexOf(data.correctOption.trim());
        
        if (selected && parseInt(selected.value) === correctIndex) {
            score++;
        }
        
        document.getElementById(`exp${index}`).style.display = 'block';
        document.querySelectorAll(`input[name="q${index}"]`).forEach(input => input.disabled = true);
    });

    alert(`পরীক্ষা সম্পন্ন হয়েছে! আপনার স্কোর: ${score}/${quizData.length}`);
    
    // শিক্ষার্থীর রেজাল্ট গুগল শিটে পাঠানোর ফাংশন (ইউজার ট্র্যাকিং)
    sendResultToSheet(score);
    
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

function sendResultToSheet(score) {
    const name = document.getElementById('student-name').value;
    const phone = document.getElementById('student-phone').value;

    fetch(GOOGLE_SHEET_SCRIPT_URL, {
        method: 'POST',
        mode: 'no-cors',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ name: name, phone: phone, score: score, total: quizData.length })
    });
}
