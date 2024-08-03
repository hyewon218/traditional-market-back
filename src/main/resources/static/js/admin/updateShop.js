$(document).ready(function() {
    // URL에서 shopNo 가져오기
    var url = window.location.href;
    var shopNo = url.substring(url.lastIndexOf('/') + 1);
    console.log("shopNo : " + shopNo);

    // 상점 정보 로드
    function loadShopinfo() {
        $.ajax({
            url: "/api/shops/" + shopNo,
            type: "GET",
            contentType: 'application/json',
            success: function(data) {
                console.log(data);

                $('#shopName').val(data.shopName);
                $('#tel').val(data.tel);
                $('#sellerName').val(data.sellerName);
                $('#shopAddr').val(data.shopAddr);
                $('#shopLat').val(data.shopLat);
                $('#shopLng').val(data.shopLng);
                $('#category').val(data.category);

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
                console.error("상점 상세 정보 불러오기 오류 : " + error);
            }
        });
    }

    loadShopinfo();

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

                // 업로드한 파일의 URL을 remainingImageUrls에 추가
                remainingImageUrls.add(e.target.result);
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

    // 수정하기 버튼 이벤트
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

        // 카테고리 값 formData에 추가
        const category = $('#category').val();
        formData.append('category', category);

        // 업로드한 파일 정보 하나씩 출력
        uploadedFiles.forEach(file => {
            console.log('uploaded Image : ', file);
        });

        // 콘솔에 남아 있는 이미지 URL 하나씩 출력
        remainingImageUrls.forEach(url => {
            console.log("Remaining Image URL :", url);
        });

        if (confirm("상점을 수정하시겠습니까?")) {
            $.ajax({
                url: '/api/shops/' + shopNo,
                type: 'PUT',
                processData: false, // FormData 사용 시 false
                contentType: false, // FormData 사용 시 false
                data: formData,
                success: function(response) {
                    console.log(response);
                    const shopNo = response.shopNo;
                    const confirmRedirect = confirm('상점이 수정되었습니다. 수정된 상점을 확인하러 가시겠습니까?');

                    if (confirmRedirect) {
                        window.location.href = '/admin/shops/' + shopNo; // 예를 선택하면 이동
                    } else {
                        window.location.href = '/admin/shops'; // 아니오 선택 시 상점 목록으로 이동
                    }
                },
                error: function(xhr) {
                    console.error("상점 수정 실패");
                    alert('상점 수정에 실패했습니다.');
                }
            });
        }
    });

    $('#findCoordinate').on('click', function() {
        const windowWidth = 570;
        const windowHeight = 600;
        const left = (window.screen.width - windowWidth) / 2;
        const top = (window.screen.height - windowHeight) / 2;
        const popupStyle = `width=${windowWidth}, height=${windowHeight}, left=${left}, top=${top}, resizable=yes, scrollbars=yes, status=yes`;

        window.open('/admin/findcoord', 'MapWindow', popupStyle);

        $(window).on('message', function(event) {
            if (event.originalEvent.origin !== window.location.origin) {
                return;
            }

            const coordinates = event.originalEvent.data;
            $('#shopLat').val(coordinates.lat);
            $('#shopLng').val(coordinates.lng);
        });
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
