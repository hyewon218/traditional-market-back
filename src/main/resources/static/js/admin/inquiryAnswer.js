$(document).ready(function() {
    // URL에서 inquiryNo 가져오기
    var url = window.location.href;
    var inquiryNo = url.substring(url.lastIndexOf('/') + 1);
    console.log("inquiryNo : " + inquiryNo);

    const uploadedFiles = new Set(); // 업로드한 파일을 저장할 Set

    // 답변하기 버튼 클릭 시 이벤트 핸들러
    $("#answerButton").click(function() {
        $("#answerForm").show();
        $("#cancelButton").show();
    });

    // 이미지 미리보기 및 파일 배열에 추가
    $('#imageFiles').on('change', function() {
        const files = this.files;

        for (let i = 0; i < files.length; i++) {
            const file = files[i];
            uploadedFiles.add(file); // Set에 파일 추가

            const reader = new FileReader();
            reader.onload = function(e) {
                $('#imagePreview').append(`
                    <div class="preview-image-container">
                        <img src="${e.target.result}" class="preview-image" onclick="openModal(this)" />
                        <span class="delete-button" data-file="${file.name}">&times;</span>
                    </div>
                `);
            }
            reader.readAsDataURL(file);
        }

        // 파일 선택 후 input 초기화 (중복 방지)
        $(this).val('');
    });

    // 이미지 삭제 함수
    $('#imagePreview').on('click', '.delete-button', function() {
        const fileName = $(this).data('file');

        // Set에서 파일 삭제
        uploadedFiles.forEach(file => {
            if (file.name === fileName) {
                uploadedFiles.delete(file);
                return false;
            }
        });

        // 화면에서 해당 이미지 제거
        $(this).closest('.preview-image-container').remove();
    });

    // 등록 버튼 클릭 시 이벤트 핸들러
    $('#submitForm').on('submit', function(event) {
        event.preventDefault();
        const answerContent = $('#answerTextarea').val().trim();
        console.log("답변칸 입력 내용 : " + answerContent);

        // 답변 내용이 비어있는지 검증
        if (answerContent === '') {
            alert('답변내용을 채워주세요.');
            return; // 함수 종료
        }

        // 확인 메시지 추가
        if (!confirm('답변을 등록하시겠습니까?')) {
            return; // 함수 종료
        }

        const formData = new FormData(document.getElementById('submitForm'));

        // Set에 있는 모든 파일 추가
        uploadedFiles.forEach(file => {
            formData.append('imageFiles', file);
        });

        // 폼 데이터 내용 로그로 출력
        for (var pair of formData.entries()) {
            console.log(pair[0] + ': ' + pair[1]);
        }

        // 답변 등록
        $.ajax({
            url: '/api/inquiryanswer/' + inquiryNo,
            type: 'POST',
            processData: false, // FormData 사용 시 false
            contentType: false, // FormData 사용 시 false
            data: formData,
            success: function(response) {
                console.log(response);
                $("#answerForm").hide();
                $("#cancelButton").hide();
                $("#answerTextarea").val('');
                $("#imageFiles").val('');
                $("#imagePreview").empty();

                location.reload(); // 현재 페이지 새로고침
            },
            error: function(xhr, status, error) {
                // 오류 처리
                console.error('답변 등록 오류:', status, error);
                alert('답변 등록 중 오류가 발생했습니다.');
            }
        });
    });

    // 취소 버튼 클릭 시 이벤트 핸들러
    $("#cancelButton").click(function() {
        $("#answerForm").hide();
        $("#cancelButton").hide();
        location.reload(); // 현재 페이지 새로고침
    });

    // 이미지 클릭 시 모달 창 표시
    $("#imageContainer").on("click", "img", function() {
        let modalImg = $(this).attr("src");
        $("#myModal").css("display", "block");
        $("#img01").attr("src", modalImg);
    });

    // 모달 창 닫기 버튼 클릭 시
    $(".close").click(function() {
        closeModal();
    });

    // 모달 영역 외 다른 곳 클릭 시 모달 창 닫기
    $(window).click(function(event) {
        if (event.target == $("#myModal")[0]) {
            closeModal();
        }
    });
});

function closeModal() {
    $("#myModal").css("display", "none");
}

