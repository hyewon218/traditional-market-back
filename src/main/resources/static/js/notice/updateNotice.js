//$(document).ready(function() {
//    // URL에서 inquiryNo 가져오기
//    var url = window.location.href;
//    var noticeNo = url.substring(url.lastIndexOf('/') + 1);
//    console.log("noticeNo : " + noticeNo);
//
//    function loadNoticeinfo() {
//        $.ajax({
//            url: "/api/notices/" + noticeNo,
//            type: "GET",
//            contentType: 'application/json',
//            success: function(data) {
//                console.log(data);
//                $('#noticeTitle').val(data.noticeTitle); // input 값으로 설정
//                $('#noticeContent').val(data.noticeContent); // textarea 값으로 설정
//
//                // 기존 이미지 로드
//                if (data.imageList && data.imageList.length > 0) {
//                    data.imageList.forEach(function(image) {
//                        $('#imagePreview').append(`
//                            <div class="preview-image-container">
//                                <img src="${image.imageUrl}" class="preview-image" onclick="openModal(this)" />
//                                <span class="delete-button" data-file="${image.imageUrl}">&times;</span>
//                            </div>
//                        `);
//                    });
//                }
//            },
//            error: function(xhr, status, error) {
//                console.error("Error fetching myinfo: " + error);
//            }
//        });
//    }
//    loadNoticeinfo();
//
//    const uploadedFiles = new Set(); // 업로드한 파일을 저장할 Set
//
//    // 이미지 미리보기 및 파일 배열에 추가
//    $('#imageFiles').on('change', function() {
//        const files = this.files;
//
//        for (let i = 0; i < files.length; i++) {
//            const file = files[i];
//            uploadedFiles.add(file); // Set에 파일 추가
//
//            const reader = new FileReader();
//            reader.onload = function(e) {
//                $('#imagePreview').append(`
//                    <div class="preview-image-container">
//                        <img src="${e.target.result}" class="preview-image" onclick="openModal(this)" />
//                        <span class="delete-button" data-file="${file.name}">&times;</span>
//                    </div>
//                `);
//            }
//            reader.readAsDataURL(file);
//        }
//
//        // 파일 선택 후 input 초기화 (중복 방지)
//        $(this).val('');
//    });
//
//    // 이미지 삭제 함수
//    $('#imagePreview').on('click', '.delete-button', function() {
//        const imageUrl = $(this).data('file');
//
//        // 화면에서 해당 이미지 제거
//        $(this).closest('.preview-image-container').remove();
//
//        // 추가된 파일 Set에서도 제거
//        uploadedFiles.forEach(file => {
//            if (file.name === imageUrl) {
//                uploadedFiles.delete(file);
//                return false;
//            }
//        });
//        // 수정화면에서 클라이언트가 삭제한 이미지의 URL 출력
//        logDeletedImage(this);
//
//        logRemainingImages();
//    });
//
//    // 이미지 삭제 시 콘솔에 기존 이미지 정보 출력
//    window.logDeletedImage = function(button) {
//        const imageUrl = $(button).data('file');
//        console.log("Deleted Image URL:", imageUrl);
//    };
//
//    // 남아 있는 이미지들의 정보 출력 함수
//    function logRemainingImages() {
//        const remainingImages = [];
//        $('.preview-image').each(function() {
//            const imageUrl = $(this).attr('src');
//            remainingImages.push(imageUrl);
//        });
//        console.log("Remaining Images:", remainingImages);
//    }
//
//    // 수정하기 버튼 클릭 이벤트
//    $('#updateForm').on('submit', function(event) {
//        event.preventDefault();
//
//        const formData = new FormData(document.getElementById('updateForm'));
//
//        // Set에 있는 모든 파일 추가
//        uploadedFiles.forEach(file => {
//            formData.append('imageFiles', file);
//        });
//
//        // 폼 데이터 내용 로그로 출력
//        for (var pair of formData.entries()) {
//            console.log(pair[0] + ': ' + pair[1]);
//        }
//
//        if (confirm("공지사항을 수정하시겠습니까?")) {
//            $.ajax({
//                url: '/api/notices/' + noticeNo,
//                type: 'PUT',
//                processData: false, // FormData 사용 시 false
//                contentType: false, // FormData 사용 시 false
//                data: formData,
//                success: function(response) {
//                    console.log(response);
//                    const noticeNo = response.noticeNo;
//                    const confirmRedirect = confirm('공지사항이 수정되었습니다. 수정된 공지사항을 확인하러 가시겠습니까?');
//
//                    if (confirmRedirect) {
//                        window.location.href = '/notice/' + noticeNo; // 예를 선택하면 이동
//                    } else {
//                        window.location.href = '/noticelist'; // 아니오 선택 시 공지사항 목록으로 이동
//                    }
//                },
//                error: function(xhr) {
//                    console.error("공지사항 수정 실패");
//                    alert('공지사항 수정에 실패했습니다.');
//                }
//            });
//        }
//    });
//
//    // 모달 열기
//    window.openModal = function(img) {
//        var modal = $('#myModal');
//        var modalImg = $('#img01');
//        var captionText = $('#caption');
//
//        modal.show();
//        modalImg.attr('src', img.src);
//        captionText.text(img.alt);
//    };
//
//    // 모달 외부 클릭 시 모달 닫기
//    $('#myModal').on('click', function(event) {
//        if (event.target === this) {
//            closeModal();
//        }
//    });
//
//    // 모달 닫기
//    window.closeModal = function() {
//        $('#myModal').hide();
//    };
//});


$(document).ready(function() {
    // URL에서 noticeNo 가져오기
    var url = window.location.href;
    var noticeNo = url.substring(url.lastIndexOf('/') + 1);
    console.log("noticeNo : " + noticeNo);

    function loadNoticeinfo() {
        $.ajax({
            url: "/api/notices/" + noticeNo,
            type: "GET",
            contentType: 'application/json',
            success: function(data) {
                console.log(data);
                $('#noticeTitle').val(data.noticeTitle); // input 값으로 설정
                $('#noticeContent').val(data.noticeContent); // textarea 값으로 설정

                // 기존 이미지 로드
                if (data.imageList && data.imageList.length > 0) {
                    data.imageList.forEach(function(image) {
                        $('#imagePreview').append(`
                            <div class="preview-image-container">
                                <img src="${image.imageUrl}" class="preview-image" onclick="openModal(this)" />
                                <span class="delete-button" data-file="${image.imageUrl}">&times;</span>
                            </div>
                        `);
                        remainingImageUrls.add(image.imageUrl);
                    });
                }
            },
            error: function(xhr, status, error) {
                console.error("Error fetching myinfo: " + error);
            }
        });
    }
    loadNoticeinfo();

    const uploadedFiles = new Set(); // 업로드한 파일을 저장할 Set
    const remainingImageUrls = new Set(); // 남아 있는 이미지 URL을 저장할 Set

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
        const imageUrl = $(this).data('file');

        // 화면에서 해당 이미지 제거
        $(this).closest('.preview-image-container').remove();

        // 추가된 파일 Set에서 제거
        uploadedFiles.forEach(file => {
            if (file.name === imageUrl) {
                uploadedFiles.delete(file);
                return false;
            }
        });

        // 남아있는 파일 Set에서 제거
        remainingImageUrls.delete(imageUrl);
    });

    // 남아 있는 이미지들의 정보 출력 함수
    function logRemainingImages() {
        const remainingImages = [];
        $('.preview-image').each(function() {
            const imageUrl = $(this).attr('src');
            remainingImages.push(imageUrl);
        });
        console.log("Remaining Images:", remainingImages);
    }

    // 수정하기 버튼 클릭 이벤트
    $('#updateForm').on('submit', function(event) {
        event.preventDefault();

        const formData = new FormData(document.getElementById('updateForm'));

        // 삭제된 이미지 외에 남아 있는 이미지들의 정보 출력
        logRemainingImages();

        // Set에 있는 모든 파일 추가
        uploadedFiles.forEach(file => {
            formData.append('imageFiles', file);
        });

        // 남아 있는 이미지 URL 추가
        remainingImageUrls.forEach(url => {
            formData.append('imageUrls', url);
        });

        // 업로드한 파일 정보 하나씩 출력
        uploadedFiles.forEach(file => {
            console.log('uploaded Image : ', file);
        });

        // 콘솔에 남아 있는 이미지 URL 하나씩 출력
        remainingImageUrls.forEach(url => {
            console.log("Remaining Image URL :", url);
        });

        if (confirm("공지사항을 수정하시겠습니까?")) {
            $.ajax({
                url: '/api/notices/' + noticeNo,
                type: 'PUT',
                processData: false, // FormData 사용 시 false
                contentType: false, // FormData 사용 시 false
                data: formData,
                success: function(response) {
                    console.log(response);
                    const noticeNo = response.noticeNo;
                    const confirmRedirect = confirm('공지사항이 수정되었습니다. 수정된 공지사항을 확인하러 가시겠습니까?');

                    if (confirmRedirect) {
                        window.location.href = '/notice/' + noticeNo; // 예를 선택하면 이동
                    } else {
                        window.location.href = '/noticelist'; // 아니오 선택 시 공지사항 목록으로 이동
                    }
                },
                error: function(xhr) {
                    console.error("공지사항 수정 실패");
                    alert('공지사항 수정에 실패했습니다.');
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
