$(document).ready(function() {
    // URL에서 inquiryNo 가져오기
    var url = window.location.href;
    var inquiryNo = url.substring(url.lastIndexOf('/') + 1);
    console.log("inquiryNo : " + inquiryNo);

    $.ajax({
        url: "/api/inquiries/" + inquiryNo,
        type: "GET",
        contentType: 'application/json',
        success: function(data) {
            console.log(data);

            $('title').text(data.inquiryTitle + " 상세보기");
            $('#h2').text(data.inquiryTitle + " 상세정보");

            // 문의사항 정보 설정
            $('#inquiryTitle').text(data.inquiryTitle);
            $('#inquiryWriter').text(data.inquiryWriter);
            $('#inquiryContent').text(data.inquiryContent);
            $('#inquiryState').text(data.inquiryState);
            $('#createTime').text(data.createTime);

            // 문의사항 상태에 따라 답변 섹션 보이기/숨기기
            if (data.inquiryState === "답변 미완료") {
                $('.answer-section').hide(); // 답변 미완료일 경우 숨김
            } else {
                $('.answer-section').show(); // 답변 완료일 경우 보임
                $('#answerButton').hide();
            }

            // 이미지 미리보기 추가
            if (data.imageList && data.imageList.length > 0) {
                data.imageList.forEach(function(image) {
                    $('#imageContainer').append(
                        '<img src="' + image.imageUrl + '" alt="문의 이미지" class="preview-image" onclick="openModal(this)">'
                    );
                });
            } else {
                $('#imageContainer').append('<p>등록된 이미지가 없습니다.</p>');
            }
        },
        error: function(xhr, status, error) {
            console.error("문의사항 상세보기 가져오는 중 오류: " + error);
        }
    });

    // 등록한 답변 출력
    $.ajax({
        url: "/api/inquiryanswer/" + inquiryNo,
        type: "GET",
        contentType: 'application/json',
        success: function(data) {
            console.log(data);

            $('#answerWriter').text(data.answerWriter);
            $('#answerCreateTime').text(data.createTime);
            $('#answerContent').text(data.answerContent);
            $('.answer-section').show();

            // 이미지 미리보기 추가
            if (data.imageList && data.imageList.length > 0) {
                data.imageList.forEach(function(image) {
                    $('#answerImageContainer').append(
                        '<img src="' + image.imageUrl + '" alt="문의 이미지" class="preview-image" onclick="openModal(this)">'
                    );
                });
            } else {
                $('#answerImageContainer').append('<p>등록된 이미지가 없습니다.</p>');
            }
        },
        error: function(xhr, status, error) {
            console.error("답변 가져오는 중 오류: " + error);
        }
    });

    // 삭제 버튼 클릭 이벤트
    $('#deleteButton').on('click', function() {
        if (confirm("정말 이 문의사항을 삭제하시겠습니까?")) {
            $.ajax({
                url: '/api/inquiries/' + inquiryNo,
                type: 'DELETE',
                success: function(response) {
                    alert('문의사항이 삭제되었습니다.');
                    window.location.href = '/myinfo/inquiries'; // 삭제 후 문의내역 페이지로 이동
                },
                error: function(xhr) {
                    alert('문의사항 삭제에 실패했습니다. 오류: ' + xhr.responseText);
                }
            });
        }
    });
});

function openModal(img) {
    var modal = $('#myModal');
    var modalImg = $('#img01');
    var captionText = $('#caption');

    modal.show();
    modalImg.attr('src', img.src);
    captionText.text(img.alt);
}

// 모달 외부 클릭 시 모달 닫기
$('#myModal').on('click', function(event) {
    if (event.target === this) {
        closeModal();
    }
});

function closeModal() {
    $('#myModal').hide();
}
