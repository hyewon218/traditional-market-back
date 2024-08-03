$(document).ready(function() {
    const uploadedFiles = new Set(); // 업로드한 파일을 저장할 Set

    // URL에서 marketNo와 marketName, shopNo, shopName을 가져오기
    const urlParams = new URLSearchParams(window.location.search);
    const marketNoFromUrl = Number(urlParams.get('marketNo')); // 숫자로 변환
    const marketNameFromUrl = urlParams.get('marketName');
    const shopNoFromUrl = Number(urlParams.get('shopNo')); // 숫자로 변환
    const shopNameFromUrl = urlParams.get('shopName');

    console.log("marketNoFromUrl:", marketNoFromUrl);
    console.log("marketNameFromUrl:", marketNameFromUrl);
    console.log("shopNoFromUrl:", shopNoFromUrl);
    console.log("shopNameFromUrl:", shopNameFromUrl);

    // 시장 목록을 가져와서 드롭다운 채우기
    function loadMarkets() {
        $.ajax({
            url: '/api/markets',
            type: 'GET',
            contentType: 'application/json',
            success: function(data) {
                const marketSelect = $('#marketNo');

                data.content.forEach(market => {
                    marketSelect.append(`
                        <option value="${market.marketNo}" ${market.marketNo === marketNoFromUrl ? 'selected' : ''}>${market.marketName}</option>
                    `);
                });

                // 드롭다운 변경 이벤트 핸들러 추가
                marketSelect.on('change', function() {
                    const marketNo = $(this).val();
                    $("#shopNo").html('<option value="all">상점 선택</option>'); // 상점 목록 초기화 및 기본값 설정
                    loadShopList(marketNo); // 상점 목록 로드
                });

                // 초기화: 페이지 로드 시 시장 번호로 상점 목록 로드
                if (marketNoFromUrl) {
                    marketSelect.val(marketNoFromUrl).change();
                }
            },
            error: function(xhr, status, error) {
                console.error("시장 목록 불러오기 오류: " + error);
            }
        });
    }

    loadMarkets();

    // 특정 시장에 해당하는 상점 목록 드롭다운 채우기
    function loadShopList(marketNo) {
        $.ajax({
            url: "/api/" + marketNo + "/shops",
            type: 'GET',
            contentType: 'application/json',
            success: function(data) {
                console.log(data);
                const shopSelect = $('#shopNo');
                shopSelect.empty(); // 기존 옵션 제거

                data.content.forEach(shop => {
                    shopSelect.append(`
                        <option value="${shop.shopNo}" ${shop.shopNo === shopNoFromUrl ? 'selected' : ''}>${shop.shopName}</option>
                    `);
                });

                // 드롭다운 변경 이벤트 핸들러 추가
                shopSelect.on('change', function() {
                    const selectedShopNo = $(this).val();
                });
            },
            error: function(xhr, status, error) {
                console.error("상점 목록 불러오기 오류: " + error);
            }
        });
    }

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

        // 화면에서 해당 이미지 제거
        $(this).closest('.preview-image-container').remove();

        // Set에서 파일 삭제
        uploadedFiles.forEach(file => {
            if (file.name === fileName) {
                uploadedFiles.delete(file);
                return false;
            }
        });
    });

    // 상품 추가 버튼 이벤트
    $('#addItemForm').on('submit', function(event) {
        event.preventDefault();

        const formData = new FormData(this);

        // Set에 있는 모든 파일 추가
        uploadedFiles.forEach(file => {
            formData.append('imageFiles', file);
        });

        // 선택된 itemCategory 값을 폼 데이터에 추가
//        const itemCategory = $('#itemCategory').val();
//        formData.append('itemCategory', itemCategory);

        if(confirm("상품을 추가하시겠습니까?")) {
            $.ajax({
                url: '/api/items',
                type: 'POST',
                processData: false, // FormData 사용 시 false
                contentType: false, // FormData 사용 시 false
                data: formData,
                success: function(response) {
                    console.log(response);
                    const itemNo = response.itemNo;
                    const confirmRedirect = confirm('상품이 추가되었습니다. 추가된 상점을 확인하러 가시겠습니까?');
                    if (confirmRedirect) {
                        window.location.href = '/items/' + itemNo; // 예를 선택하면 이동
                    } else {
                        window.location.href = '/admin/items'; // 아니오 선택 시 상점 관리 페이지로 이동
                    }
                },
                error: function(xhr) {
                    alert('상품 추가에 실패했습니다.');
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
