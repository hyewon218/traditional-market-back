// 전역 변수로 저장된 메시지 목록 선언
let savedMessages = null;

// 모달 열기
function openModal() {
    const modal = document.getElementById("myModal");
    modal.style.display = "block";

    $.ajax({
        url: "/api/deliverymessage",
        type: "GET",
        contentType: "application/json",
        success: function(response) {
            console.log(response);
            savedMessages = response; // 저장된 메시지 목록을 전역 변수에 저장

            // 기존 버튼들 초기화
            $('#savedMessages').empty();

            // 모든 저장된 메시지 버튼을 생성
            savedMessages.forEach(function(message) {
                var button = '<button onclick="selectSavedMessage(\'' + message.no + '\', \'' + message.content + '\')"> (저장됨) ' + message.content + '</button>';
                $('#savedMessages').append(button);
            });
        },
        error: function(xhr) {
            console.error("저장된 배송 메시지 목록 불러오기 실패:", xhr.responseText);
        }
    });
}

// 모달 닫기
function closeModal() {
    const modal = document.getElementById("myModal");
    modal.style.display = "none";
}

// 관리 모달 열기
function openManageModal() {
    closeModal(); // 기존 모달 닫기

    $.ajax({
        url: "/api/deliverymessage",
        type: "GET",
        contentType: "application/json",
        success: function(response) {
            console.log(response);
            savedMessages = response; // 저장된 메시지 목록을 전역 변수에 저장

            // 관리 모달의 내용을 설정
            var manageModalContent = document.getElementById("manageModalContent");
            manageModalContent.innerHTML = ''; // 기존 내용 초기화

            savedMessages.forEach(function(message) {
                var messageElement = document.createElement('div');
                messageElement.className = 'message-item'; // 스타일을 위한 클래스 추가
                messageElement.innerHTML = `
                    <span>${message.content}</span>
                    <button class="delete-button" onclick="deleteMessage('${message.no}')">삭제</button>
                `;
                manageModalContent.appendChild(messageElement);
            });

            // 관리 모달 열기
            const manageModal = document.getElementById("manageModal");
            manageModal.style.display = "block";
        },
        error: function(xhr) {
            console.error("저장된 배송 메시지 목록 불러오기 실패:", xhr.responseText);
        }
    });
}

// 관리 모달 닫기
function closeManageModal() {
    const manageModal = document.getElementById("manageModal");
    manageModal.style.display = "none";
    openModal();
}

// 선택안함
function selectNone() {
    $("#saveCustomMessageBtn").hide();
    document.getElementById("selectDeliveryBtn").innerText = "선택안함";
    document.getElementById("customMessageTextarea").value = "";
    document.getElementById("customMessageTextarea").classList.add("hidden");
    closeModal();
}

// 직접 입력하기
function enterDirectly() {
    document.getElementById("selectDeliveryBtn").innerText = "직접 입력하기";
    document.getElementById("customMessageTextarea").value = "";
    document.getElementById("customMessageTextarea").classList.remove("hidden");
    $("#saveCustomMessageBtn").show();

    // '저장' 버튼 클릭 시 이벤트 처리
    $("#saveCustomMessageBtn").click(function() {
        if (confirm("배송메시지를 저장하시겠습니까?")) {
            $.ajax({
                url: "/api/deliverymessage",
                type: "POST",
                contentType: "application/json",
                data: JSON.stringify({
                    content: $("#customMessageTextarea").val()
                }),
                success: function(response) {
                    console.log("배송메시지 저장 성공:", response);
                    alert("배송메시지 저장 성공");
                },
                error: function(xhr) {
                    console.error("배송메시지 저장 실패:", xhr.responseText);
                    alert("배송메시지 저장 실패");
                }
            });
        }
    });

    closeModal();
}
// 삭제 버튼 클릭 시 호출되는 함수
function deleteMessage(deliveryMessageNo) {
    console.log('삭제 요청: ', deliveryMessageNo);

    if(confirm("해당 배송메시지를 삭제하시겠습니까?")) {
        $.ajax({
            url: `/api/deliverymessage/${deliveryMessageNo}`,
            type: "DELETE",
            success: function(response) {
                console.log("배송메시지 삭제 성공:", response);
                closeManageModal();
                openModal();
            },
            error: function(xhr) {
                console.error("배송메시지 삭제 실패:", xhr.responseText);
            }
        });
    }
}

// 공통 메시지 1 선택
function selectCommon1() {
    $("#saveCustomMessageBtn").hide();
    const commonMessage = "문 앞에 놓아주세요";
    document.getElementById("selectDeliveryBtn").innerText = commonMessage;
    document.getElementById("customMessageTextarea").value = commonMessage;
    document.getElementById("customMessageTextarea").classList.add("hidden");
    closeModal();
}

// 공통 메시지 2 선택
function selectCommon2() {
    $("#saveCustomMessageBtn").hide();
    const commonMessage = "부재 시 연락 부탁드려요";
    document.getElementById("selectDeliveryBtn").innerText = commonMessage;
    document.getElementById("customMessageTextarea").value = commonMessage;
    document.getElementById("customMessageTextarea").classList.add("hidden");
    closeModal();
}

// 공통 메시지 3 선택
function selectCommon3() {
    $("#saveCustomMessageBtn").hide();
    const commonMessage = "배송 전 미리 연락해주세요";
    document.getElementById("selectDeliveryBtn").innerText = commonMessage;
    document.getElementById("customMessageTextarea").value = commonMessage;
    document.getElementById("customMessageTextarea").classList.add("hidden");
    closeModal();
}

// 저장된 메시지 선택
function selectSavedMessage(messageNo, messageContent) {
    $("#saveCustomMessageBtn").hide();
    document.getElementById("selectDeliveryBtn").innerText = "요청사항 직접 입력하기";
    document.getElementById("customMessageTextarea").value = messageContent;
    document.getElementById("customMessageTextarea").classList.remove("hidden");
    closeModal();
}

// 모달 외부 클릭 시 모달 닫기
$('#myModal').on('click', function(event) {
    if (event.target === this) {
        closeModal();
    }
});
// 관리 버튼 모달 외부 클릭 시 모달 닫기
$('#manageModal').on('click', function(event) {
    if (event.target === this) {
        closeManageModal();
    }
});
