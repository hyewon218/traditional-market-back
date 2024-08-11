$(document).ready(function() {
    const uploadedFiles = new Set(); // 업로드한 파일을 저장할 Set

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

    $('#noticeForm').on('submit', function(event) {
        event.preventDefault();

        const formData = new FormData(this);

        // Set에 있는 모든 파일 추가
        uploadedFiles.forEach(file => {
            formData.append('imageFiles', file);
        });

        if (confirm("공지사항을 작성하시겠습니까?")) {
            $.ajax({
                url: '/api/notices',
                type: 'POST',
                processData: false, // FormData 사용 시 false
                contentType: false, // FormData 사용 시 false
                data: formData,
                success: function(response) {
                    console.log(response);
                    const noticeNo = response.noticeNo;
                    const confirmRedirect = confirm('공지사항이 작성되었습니다. 작성된 공지사항을 확인하러 가시겠습니까?');

                    if (confirmRedirect) {
                        window.location.href = '/notice/' + noticeNo; // 예를 선택하면 이동
                    } else {
//                        window.location.href = '/noticelist';
                        window.location.href = '/admin/notices';
                    }
                },
                error: function(xhr) {
                    alert('공지사항 작성에 실패했습니다.');
                }
            });
        }
    });

    // 모달 열기
    window.openModal = function(img) {
        var modal = $('#myModal');
        var modalImg = $('#img01');
        var captionText = $('#caption');

        modal.show();
        modalImg.attr('src', img.src);
        captionText.text(img.alt);
    };

    // 모달 외부 클릭 시 모달 닫기
    $('#myModal').on('click', function(event) {
        if (event.target === this) {
            closeModal();
        }
    });

    // 모달 닫기
    window.closeModal = function() {
        $('#myModal').hide();
    };
});

