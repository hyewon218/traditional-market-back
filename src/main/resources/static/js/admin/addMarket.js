$(document).ready(function() {
    const uploadedFiles = new Set(); // 업로드한 파일을 저장할 Set
    const defaultImageUrl = 'https://upgrade-aws-config-storage.s3.ap-northeast-2.amazonaws.com/%E1%84%89%E1%85%B5%E1%84%8C%E1%85%A1%E1%86%BC%E1%84%80%E1%85%B5%E1%84%87%E1%85%A9%E1%86%AB%E1%84%8B%E1%85%B5%E1%84%86%E1%85%B5%E1%84%8C%E1%85%B5.jpg';

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

    // 시장 추가 버튼 이벤트
    $('#addMarketForm').on('submit', function(event) {
        event.preventDefault();

        const formData = new FormData(this);

        // Set에 있는 모든 파일 추가
        uploadedFiles.forEach(file => {
            formData.append('imageFiles', file);
        });

        const category = $('#category').val();
        formData.append('category', category);

        if(confirm("시장을 추가하시겠습니까?")) {
            $.ajax({
                url: '/api/markets',
                type: 'POST',
                processData: false, // FormData 사용 시 false
                contentType: false, // FormData 사용 시 false
                data: formData,
                success: function(response) {
                    console.log(response);
                    const marketNo = response.marketNo;
                    const confirmRedirect = confirm('시장이 추가되었습니다. 추가된 시장을 확인하러 가시겠습니까?');
                    if (confirmRedirect) {
                        window.location.href = '/markets/' + marketNo; // 예를 선택하면 이동
                    } else {
                        window.location.href = '/admin/markets'; // 아니오 선택 시 시장 관리 페이지로 이동
                    }
                },
                error: function(xhr) {
                    alert('시장 추가에 실패했습니다.');
                }
            });
        }
    });

    // 좌표 찾기 버튼 클릭 시 지도 띄우고 각각의 필드에 맞게 값 추가하기
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

            var data = event.originalEvent.data;
            console.log("data : " + data)

            if (data.type === 'UPDATE_BUS_COORDS') {
                document.getElementById('busLat').value = data.coords.lat;
                document.getElementById('busLng').value = data.coords.lng;
            } else if (data.type === 'UPDATE_SUBWAY_COORDS') {
                document.getElementById('subwayLat').value = data.coords.lat;
                document.getElementById('subwayLng').value = data.coords.lng;
            }
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

