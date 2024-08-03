$(document).ready(function() {
    // URL에서 itemNo 가져오기
    var url = window.location.href;
    var itemNo = url.substring(url.lastIndexOf('/') + 1);
    console.log("itemNo : " + itemNo);

    // 상품 정보 로드
    function loadIteminfo() {
        $.ajax({
            url: "/api/items/" + itemNo,
            type: "GET",
            contentType: 'application/json',
            success: function(data) {
                console.log(data);

                $('#itemName').val(data.itemName);
                $('#price').val(data.price);
                $('#stockNumber').val(data.stockNumber);
                $('#itemDetail').val(data.itemDetail);
                $('#itemSellStatus').val(data.itemSellStatus);

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
                console.error("상품 상세 정보 불러오기 오류 : " + error);
            }
        });
    }

    loadIteminfo();

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
        const itemSellStatus = $('#itemSellStatus').val();
        formData.append('itemSellStatus', itemSellStatus);

        // 업로드한 파일 정보 하나씩 출력
        uploadedFiles.forEach(file => {
            console.log('uploaded Image : ', file);
        });

        // 콘솔에 남아 있는 이미지 URL 하나씩 출력
        remainingImageUrls.forEach(url => {
            console.log("Remaining Image URL :", url);
        });

        if (confirm("상품을 수정하시겠습니까?")) {
            $.ajax({
                url: '/api/items/' + itemNo,
                type: 'PUT',
                processData: false, // FormData 사용 시 false
                contentType: false, // FormData 사용 시 false
                data: formData,
                success: function(response) {
                    console.log(response);
                    const itemNo = response.itemNo;
                    const confirmRedirect = confirm('상픔이 수정되었습니다. 수정된 상점을 확인하러 가시겠습니까?');

                    if (confirmRedirect) {
                        window.location.href = '/items/' + itemNo; // 예를 선택하면 이동
                    } else {
                        window.location.href = '/admin/items'; // 아니오 선택 시 상품 목록으로 이동
                    }
                },
                error: function(xhr) {
                    console.error("상품 수정 실패");
                    alert('상품 수정에 실패했습니다.');
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
