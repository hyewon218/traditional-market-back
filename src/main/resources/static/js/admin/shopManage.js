$(document).ready(function() {
    let currentPage = 0; // 현재 페이지 초기화
    const groupSize = 5; // 한 그룹에 보여줄 페이지 수
    let selectedCategory = ""; // 선택된 카테고리 초기화
    let sortBy = "shopName,desc"; // 기본 정렬 기준

    loadMarkets();
    loadAllShops(currentPage, selectedCategory);

    // 카테고리와 한글 이름을 매핑한 객체
    const categoryTranslations = {
        "AGRI": "농산물",
        "MARINE": "수산물",
        "LIVESTOCK": "축산물",
        "FRUITS": "과일",
        "PROCESSED": "가공식품",
        "RICE": "쌀",
        "RESTAURANT": "식당",
        "SIDEDISH": "반찬",
        "STUFF": "잡화",
        "ETC": "기타"
    };

    // 카테고리 버튼 생성
    const categories = ["AGRI", "MARINE", "LIVESTOCK", "FRUITS", "PROCESSED", "RICE", "RESTAURANT", "SIDEDISH", "STUFF", "ETC"];
    categories.forEach(category => {
        const categoryName = categoryTranslations[category] || category; // 매핑된 한글 이름 가져오기
        $(".shopList-category").append(`<button class="category-btn" data-category="${category}">${categoryName}</button>`);
    });

    // 조회수순 버튼 클릭 이벤트
    $("#viewCountButton").click(function() {
        sortBy = "viewCount,desc"; // 조회수 내림차순으로 정렬 기준 설정
        const selectedMarketNo = $('#marketNo').val();

        if (selectedMarketNo === 'all') {
            loadAllShops(currentPage, selectedCategory);
        } else if(selectedMarketNo) {
            loadShopList(selectedMarketNo, currentPage, selectedCategory);
        }
    });

    // 시장 목록을 가져와서 드롭다운 채우기
    function loadMarkets() {
        $.ajax({
            url: '/api/markets',
            type: 'GET',
            contentType: 'application/json',
            success: function(data) {
                const marketSelect = $('#marketNo');
                marketSelect.empty(); // 기존 옵션 제거
                marketSelect.append('<option value="all">전체보기</option>'); // 전체보기 옵션 추가

                data.content.forEach(market => {
                    marketSelect.append(`
                        <option value="${market.marketNo}">${market.marketName}</option>
                    `);
                });

                // 드롭다운 변경 이벤트 핸들러 추가
                marketSelect.on('change', function() {
                    const selectedMarketNo = $(this).val();
                    searchQuery = "";
                    sortBy = "shopName,desc";
                    if (selectedMarketNo === 'all') {
                        loadAllShops(currentPage, selectedCategory);
                    } else if (selectedMarketNo) {
                        loadShopList(selectedMarketNo, currentPage, selectedCategory);
                    } else {
                        $(".shopList-contents").html('<h2>상점 관리</h2><p>시장 선택이 필요합니다.</p>');
                    }
                });
            },
            error: function(xhr, status, error) {
                console.error("시장 목록 불러오기 오류: " + error);
            }
        });
    }

    // 특정 시장에 해당하는 상점 목록을 가져오는 함수
    function loadShopList(marketNo, page, category) {
        let url = `/api/${marketNo}/shops?page=${page}&size=3&sort=${sortBy}`;
        if (category) {
            url = `/api/${marketNo}/shops/category?page=${page}&size=3&sort=${sortBy}&category=${category}`;
        }

        $.ajax({
            url: url,
            type: "GET",
            success: function(data) {
                let contentHtml = `<h2>상점 관리</h2>`;

                if (data.content.length === 0) {
                    $(".shopList-contents").html(`
                        <h2>상점 관리</h2>
                        <p>선택한 카테고리에 해당하는 상점이 존재하지 않습니다</p>
                        <button id="viewAllShopsBtn">전체 상점 목록 보기</button>
                    `);

                    // 전체 상점 목록 보기 버튼 클릭 이벤트 핸들러 추가
                    $("#viewAllShopsBtn").click(function() {
                        // 페이지 새로고침
                        location.reload();
                    });
                    return;
                }

                let shopTable = `
                    <table>
                        <thead>
                            <tr>
                                <th>상점 이름</th>
                                <th>소속 시장</th> <!-- '소속 시장' 필드 추가 -->
                                <th>전화번호</th>
                                <th>분류</th>
                                <th>조회수</th>
                                <th>좋아요 수</th>
                                <th>수정</th>
                                <th>삭제</th>
                            </tr>
                        </thead>
                        <tbody>`;

                const marketNames = {};

                // 상점의 소속 시장 이름을 저장하기 위한 API 호출
                function fetchMarketName(marketNo, callback) {
                    if (marketNames[marketNo]) {
                        callback(marketNames[marketNo]);
                        return;
                    }

                    $.ajax({
                        url: `/api/markets/${marketNo}`,
                        type: "GET",
                        success: function(data) {
                            marketNames[marketNo] = data.marketName;
                            callback(data.marketName);
                        },
                        error: function(xhr) {
                            console.error("시장 정보 불러오기 오류: " + xhr.responseText);
                            callback('정보 없음');
                        }
                    });
                }

                // 상점 목록의 각 상점에 대해 소속 시장 이름을 가져온 후 테이블에 추가
                let remainingRequests = data.content.length;
                data.content.forEach(function(shop) {
                    fetchMarketName(shop.marketNo, function(marketName) {
                        let shopUrl = "/admin/shops/" + shop.shopNo;
                        let categoryName;

                        switch (shop.category) {
                            case 'AGRI': categoryName = '농산물'; break;
                            case 'MARINE': categoryName = '수산물'; break;
                            case 'LIVESTOCK': categoryName = '축산물'; break;
                            case 'FRUITS': categoryName = '과일'; break;
                            case 'PROCESSED': categoryName = '가공식품'; break;
                            case 'RICE': categoryName = '쌀'; break;
                            case 'RESTAURANT': categoryName = '식당'; break;
                            case 'SIDEDISH': categoryName = '반찬'; break;
                            case 'STUFF': categoryName = '잡화'; break;
                            case 'ETC': categoryName = '기타'; break;
                        }

                        let marketUrl = "/admin/markets/" + shop.marketNo;

                        shopTable += `
                            <tr>
                                <td><a href="${shopUrl}" class="shop-title">${shop.shopName}</a></td>
                                <td><a href="${marketUrl}">${marketName}</a></td>
                                <td>${shop.tel}</td>
                                <td>${categoryName}</td>
                                <td>${shop.viewCount}</td>
                                <td>${shop.likes}</td>
                                <td>
                                    <button class="update-shop" data-shop-no="${shop.shopNo}">
                                        <a href="/admin/shops/u/${shop.shopNo}">수정</a>
                                    </button>
                                </td>
                                <td>
                                    <button class="delete-shop" data-shop-no="${shop.shopNo}">삭제</button>
                                </td>
                            </tr>`;

                        remainingRequests--;
                        if (remainingRequests === 0) {
                            shopTable += `
                                </tbody>
                            </table>`;

                            contentHtml += shopTable;
                            $(".shopList-contents").html(contentHtml);

                            $(".delete-shop").on("click", function() {
                                const shopNo = $(this).data("shop-no");
                                $('#adminPw').val('');
                                $("#checkPwError").text('');
                                $('#verifyPwModal').show();

                                $('#deleteExecuteBtn').off('click').on('click', function() {
                                    var adminPw = $('#adminPw').val();

                                    if (!$('#verifyPwForm')[0].checkValidity()) {
                                        alert("비밀번호를 입력해주세요");
                                        return;
                                    }

                                    $.ajax({
                                        url: "/api/members/myinfo/check",
                                        type: "POST",
                                        data: { password: adminPw },
                                        success: function(data) {
                                            if (confirm("정말 이 상점을 삭제하시겠습니까?")) {
                                                $.ajax({
                                                    url: "/api/shops/" + shopNo,
                                                    type: "DELETE",
                                                    success: function(response) {
                                                        alert(response.message);
                                                        $('#verifyPwModal').hide();
                                                        loadShopList($('#marketNo').val(), currentPage, selectedCategory);
                                                    },
                                                    error: function(xhr) {
                                                        alert('삭제 실패: ' + xhr.responseText);
                                                    }
                                                });
                                            }
                                        },
                                        error: function(xhr) {
                                            const errorResponse = JSON.parse(xhr.responseText);
                                            const errorMsg = errorResponse.message;
                                            $("#checkPwError").text(errorMsg);
                                        }
                                    });
                                });
                            });

                            renderPagination(data.totalPages);
                        }
                    });
                });
            },
            error: function(xhr) {
                $(".shopList-contents").html(`<p>${xhr.responseText}</p>`);
            }
        });
    }

    // 전체 상점 목록을 가져오는 함수
    function loadAllShops(page, category) {
        let url = `/api/shops?page=${page}&size=3&sort=${sortBy}`;
        if (category) {
            url = `/api/shops/category?page=${page}&size=3&sort=${sortBy}&category=${category}`;
        }

        $.ajax({
            url: url,
            type: "GET",
            success: function(data) {
                let contentHtml = `<h2>상점 관리</h2>`;

                if (data.content.length === 0) {
                    $(".shopList-contents").html(`
                        <h2>상점 관리</h2>
                        <p>선택한 카테고리에 해당하는 상점이 존재하지 않습니다</p>
                        <button id="viewAllShopsBtn">전체 상점 목록 보기</button>
                    `);

                    // 전체 상점 목록 보기 버튼 클릭 이벤트 핸들러 추가
                    $("#viewAllShopsBtn").click(function() {
                        // 페이지 새로고침
                        location.reload();
                    });
                    return;
                }

                let shopTable = `
                    <table>
                        <thead>
                            <tr>
                                <th>상점 이름</th>
                                <th>소속 시장</th> <!-- '소속 시장' 필드 추가 -->
                                <th>전화번호</th>
                                <th>분류</th>
                                <th>조회수</th>
                                <th>좋아요 수</th>
                                <th>수정</th>
                                <th>삭제</th>
                            </tr>
                        </thead>
                        <tbody>`;

                const marketNames = {};

                // 상점의 소속 시장 이름을 저장하기 위한 API 호출
                function fetchMarketName(marketNo, callback) {
                    if (marketNames[marketNo]) {
                        callback(marketNames[marketNo]);
                        return;
                    }

                    $.ajax({
                        url: `/api/markets/${marketNo}`,
                        type: "GET",
                        success: function(data) {
                            marketNames[marketNo] = data.marketName;
                            callback(data.marketName);
                        },
                        error: function(xhr) {
                            console.error("시장 정보 불러오기 오류: " + xhr.responseText);
                            callback('정보 없음');
                        }
                    });
                }

                // 상점 목록의 각 상점에 대해 소속 시장 이름을 가져온 후 테이블에 추가
                let remainingRequests = data.content.length;
                data.content.forEach(function(shop) {
                    fetchMarketName(shop.marketNo, function(marketName) {
                        let shopUrl = "/admin/shops/" + shop.shopNo;
                        let categoryName;

                        switch (shop.category) {
                            case 'AGRI': categoryName = '농산물'; break;
                            case 'MARINE': categoryName = '수산물'; break;
                            case 'LIVESTOCK': categoryName = '축산물'; break;
                            case 'FRUITS': categoryName = '과일'; break;
                            case 'PROCESSED': categoryName = '가공식품'; break;
                            case 'RICE': categoryName = '쌀'; break;
                            case 'RESTAURANT': categoryName = '식당'; break;
                            case 'SIDEDISH': categoryName = '반찬'; break;
                            case 'STUFF': categoryName = '잡화'; break;
                            case 'ETC': categoryName = '기타'; break;
                        }

                        let marketUrl = "/admin/markets/" + shop.marketNo;

                        shopTable += `
                            <tr>
                                <td><a href="${shopUrl}" class="shop-title">${shop.shopName}</a></td>
                                <td><a href="${marketUrl}">${marketName}</a></td>
                                <td>${shop.tel}</td>
                                <td>${categoryName}</td>
                                <td>${shop.viewCount}</td>
                                <td>${shop.likes}</td>
                                <td>
                                    <button class="update-shop" data-shop-no="${shop.shopNo}">
                                        <a href="/admin/shops/u/${shop.shopNo}">수정</a>
                                    </button>
                                </td>
                                <td>
                                    <button class="delete-shop" data-shop-no="${shop.shopNo}">삭제</button>
                                </td>
                            </tr>`;

                        remainingRequests--;
                        if (remainingRequests === 0) {
                            shopTable += `
                                </tbody>
                            </table>`;

                            contentHtml += shopTable;
                            $(".shopList-contents").html(contentHtml);

                            $(".delete-shop").on("click", function() {
                                const shopNo = $(this).data("shop-no");
                                $('#adminPw').val('');
                                $("#checkPwError").text('');
                                $('#verifyPwModal').show();

                                $('#deleteExecuteBtn').off('click').on('click', function() {
                                    var adminPw = $('#adminPw').val();

                                    if (!$('#verifyPwForm')[0].checkValidity()) {
                                        alert("비밀번호를 입력해주세요");
                                        return;
                                    }

                                    $.ajax({
                                        url: "/api/members/myinfo/check",
                                        type: "POST",
                                        data: { password: adminPw },
                                        success: function(data) {
                                            if (confirm("정말 이 상점을 삭제하시겠습니까?")) {
                                                $.ajax({
                                                    url: "/api/shops/" + shopNo,
                                                    type: "DELETE",
                                                    success: function(response) {
                                                        alert(response.message);
                                                        $('#verifyPwModal').hide();
                                                        loadAllShops(currentPage, selectedCategory);
                                                    },
                                                    error: function(xhr) {
                                                        alert('삭제 실패: ' + xhr.responseText);
                                                    }
                                                });
                                            }
                                        },
                                        error: function(xhr) {
                                            const errorResponse = JSON.parse(xhr.responseText);
                                            const errorMsg = errorResponse.message;
                                            $("#checkPwError").text(errorMsg);
                                        }
                                    });
                                });
                            });

                            renderPagination(data.totalPages);
                        }
                    });
                });
            },
            error: function(xhr) {
                $(".shopList-contents").html(`<p>${xhr.responseText}</p>`);
            }
        });
    }

    function renderPagination(totalPages) {
        const paginationContainer = $(".pagination");
        paginationContainer.empty();

        const currentGroup = Math.floor(currentPage / groupSize);

        paginationContainer.append('<button class="first-group">처음</button>');

        if (currentGroup > 0) {
            paginationContainer.append('<button class="prev-group">이전</button>');
        }

        const startPage = currentGroup * groupSize;
        const endPage = Math.min(startPage + groupSize - 1, totalPages - 1);

        for (let i = startPage; i <= endPage; i++) {
            const isActive = i === currentPage;
            const buttonClass = isActive ? 'active' : '';
            paginationContainer.append(`
                <button class="page-btn ${buttonClass}" data-page="${i}">${i + 1}</button>
            `);
        }

        if (endPage < totalPages - 1) {
            paginationContainer.append('<button class="next-group">다음</button>');
        }

        paginationContainer.append('<button class="last-group">끝</button>');

        $(".page-btn").click(function() {
            const newPage = $(this).data("page");
            currentPage = newPage;
            $(".page-btn").removeClass('active');
            $(this).addClass('active');
            const selectedMarketNo = $('#marketNo').val();
            if (selectedMarketNo === 'all') {
                loadAllShops(newPage, selectedCategory);
            } else if (selectedMarketNo) {
                loadShopList(selectedMarketNo, currentPage, selectedCategory);
            }
        });

        $(".next-group").click(function() {
            if (currentPage < totalPages - 1) {
                currentPage++;
                $(".page-btn").removeClass('active');
                $(`.page-btn[data-page="${currentPage}"]`).addClass('active');
                const selectedMarketNo = $('#marketNo').val();
                if (selectedMarketNo === 'all') {
                    loadAllShops(currentPage, selectedCategory);
                } else if (selectedMarketNo) {
                    loadShopList(selectedMarketNo, currentPage, selectedCategory);
                }
            }
        });

        $(".prev-group").click(function() {
            if (currentPage > 0) {
                currentPage--;
                $(".page-btn").removeClass('active');
                $(`.page-btn[data-page="${currentPage}"]`).addClass('active');
                const selectedMarketNo = $('#marketNo').val();
                if (selectedMarketNo === 'all') {
                    loadAllShops(currentPage, selectedCategory);
                } else if (selectedMarketNo) {
                    loadShopList(selectedMarketNo, currentPage, selectedCategory);
                }
            }
        });

        $(".first-group").click(function() {
            currentPage = 0;
            $(".page-btn").removeClass('active');
            $(`.page-btn[data-page="${currentPage}"]`).addClass('active');
            const selectedMarketNo = $('#marketNo').val();
            if (selectedMarketNo === 'all') {
                loadAllShops(currentPage, selectedCategory);
            } else if (selectedMarketNo) {
                loadShopList(selectedMarketNo, currentPage, selectedCategory);
            }
        });

        $(".last-group").click(function() {
            currentPage = totalPages - 1;
            $(".page-btn").removeClass('active');
            $(`.page-btn[data-page="${currentPage}"]`).addClass('active');
            const selectedMarketNo = $('#marketNo').val();
            if (selectedMarketNo === 'all') {
                loadAllShops(currentPage, selectedCategory);
            } else if (selectedMarketNo) {
                loadShopList(selectedMarketNo, currentPage, selectedCategory);
            }
        });
    }

    // 카테고리 버튼 클릭 이벤트 핸들러
    $(".shopList-category").on("click", ".category-btn", function() {
        selectedCategory = $(this).data("category");
        currentPage = 0; // 페이지 번호 초기화
        const selectedMarketNo = $('#marketNo').val();
        if (selectedMarketNo === 'all') {
            loadAllShops(currentPage, selectedCategory);
        } else if (selectedMarketNo) {
            loadShopList(selectedMarketNo, currentPage, selectedCategory);
        }
    });

    // 모달 닫기
    $('.close').on('click', function() {
        $('#verifyPwModal').hide();
    });
});
