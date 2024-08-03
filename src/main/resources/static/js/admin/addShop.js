$(document).ready(function() {
    const uploadedFiles = new Set(); // 업로드한 파일을 저장할 Set

    // URL에서 marketNo와 marketName을 가져오기
    const urlParams = new URLSearchParams(window.location.search);
    const marketNoFromUrl = Number(urlParams.get('marketNo')); // 숫자로 변환
    const marketNameFromUrl = urlParams.get('marketName');

    console.log("marketNoFromUrl:", marketNoFromUrl);
    console.log("marketNameFromUrl:", marketNameFromUrl);

    // 시장 목록을 가져와서 드롭다운 채우기
    function loadMarkets() {
        $.ajax({
            url: '/api/markets',
            type: 'GET',
            contentType: 'application/json',
            success: function(data) {
                console.log(data);
                const marketSelect = $('#marketNo');
                marketSelect.empty(); // 기존 옵션 제거
                marketSelect.append('<option value="">시장 선택</option>'); // 기본 선택 옵션 추가

                data.content.forEach(market => {
                    console.log(`Checking marketNo: ${market.marketNo} against ${marketNoFromUrl}`);
                    marketSelect.append(`
                        <option value="${market.marketNo}" ${market.marketNo === marketNoFromUrl ? 'selected' : ''}>${market.marketName}</option>
                    `);
                });
            },
            error: function(xhr, status, error) {
                console.error("시장 목록 불러오기 오류: " + error);
            }
        });
    }

    loadMarkets(); // 시장 목록 로드 호출

    // 페이지 로드 시 URL에서 marketNo와 marketName 설정
    if (marketNoFromUrl) {
        $('#marketNo').val(marketNoFromUrl);
    }
    if (marketNameFromUrl) {
        $('#marketName').val(marketNameFromUrl);
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

    // 상점 추가 버튼 이벤트
    $('#addShopForm').on('submit', function(event) {
        event.preventDefault();

        const formData = new FormData(this);

        // Set에 있는 모든 파일 추가
        uploadedFiles.forEach(file => {
            formData.append('imageFiles', file);
        });

        // 선택된 marketNo 값을 폼 데이터에 추가
        const marketNo = $('#marketNo').val();
        formData.append('marketNo', marketNo);

        // 선택된 category 값을 폼 데이터에 추가
        const category = $('#category').val();
        formData.append('category', category);

        if(confirm("상점을 추가하시겠습니까?")) {
            $.ajax({
                url: '/api/shops',
                type: 'POST',
                processData: false, // FormData 사용 시 false
                contentType: false, // FormData 사용 시 false
                data: formData,
                success: function(response) {
                    console.log(response);
                    const shopNo = response.shopNo;
                    const confirmRedirect = confirm('상점이 추가되었습니다. 추가된 상점을 확인하러 가시겠습니까?');
                    if (confirmRedirect) {
                        window.location.href = '/shops/' + shopNo; // 예를 선택하면 이동
                    } else {
                        window.location.href = '/admin/shops'; // 아니오 선택 시 상점 관리 페이지로 이동
                    }
                },
                error: function(xhr) {
                    alert('상점 추가에 실패했습니다.');
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

