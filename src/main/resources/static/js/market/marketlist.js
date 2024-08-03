//$(document).ready(function() {
//    let currentPage = 0;
//    const groupSize = 5; // 한 그룹에 보여줄 페이지 수
//
//    // 페이지가 로드될 때 첫 번째 페이지의 시장 목록 가져오기
//    loadMarkets(currentPage);
//
//    // 시장 목록을 가져오는 함수
//    function loadMarkets(page) {
//        $.ajax({
//            url: "/api/markets?page=" + page + "&size=5&sort=createTime,asc", // 예시 URL, 실제 API URL로 변경 필요
//            type: "GET",
//            success: function(data) {
//                console.log(data);
//
//                // 시장 목록 제목을 유지
//                let contentHtml = `<h2>시장 찾기</h2>`;
//
//                // 시장이 없을 경우
//                if (data.content.length === 0) {
//                    $(".marketList-contents").html(`
//                        <h2>시장 찾기</h2>
//                        <p>시장이 존재하지 않습니다</p>
//                    `);
//                    $(".pagination").remove(); // 페이지네이션이 있으면 제거
//                    return; // 함수 실행 종료
//                }
//
//                let marketTable = `
//                    <table>
//                        <thead>
//                            <tr>
//                                <th>시장 이름</th>
//                                <th>주소</th>
//                                <th>상세 설명</th>
//                                <th>좋아요 수</th>
//                               <!-- <th>이미지</th> -->
//                            </tr>
//                        </thead>
//                        <tbody>`;
//
//                data.content.forEach(function(market) {
//                    let marketUrl = "/markets/" + market.marketNo;
//                    marketTable += `
//                        <tr>
//                            <td><a href="${marketUrl}" class="market-name">${market.marketName}</a></td>
//                            <td>${market.marketAddr}</td>
//                            <td>${market.marketDetail}</td>
//                            <td>${market.likes}</td>
//                            <!--
//                            <td>
//                                ${market.imageList.length > 0 ? `<img src="${market.imageList[0].imageUrl}" alt="${market.marketName} 이미지">` : '이미지 없음'}
//                            </td>
//                            -->
//                        </tr>`;
//                });
//
//                marketTable += `
//                        </tbody>
//                    </table>`;
//
//                // 전체 내용 설정
//                contentHtml += marketTable;
//                $(".marketList-contents").html(contentHtml); // marketList-contents에 테이블 추가
//
//                // 페이지네이션 렌더링
//                renderPagination(data.totalPages);
//            },
//            error: function(xhr) {
//                $(".marketList-contents").html(`<p>${xhr.responseText}</p>`);
//            }
//        });
//    }
//
//    function renderPagination(totalPages) {
//        const paginationContainer = $(".pagination");
//        paginationContainer.empty();
//
//        // 현재 페이지를 기준으로 현재 그룹 계산
//        const currentGroup = Math.floor(currentPage / groupSize);
//
//        // 처음 버튼
//        paginationContainer.append('<button class="first-group">처음</button>');
//
//        // 이전 그룹 버튼
//        if (currentGroup > 0) {
//            paginationContainer.append('<button class="prev-group">이전</button>');
//        }
//
//        // 페이지 버튼
//        const startPage = currentGroup * groupSize;
//        const endPage = Math.min(startPage + groupSize - 1, totalPages - 1);
//
//        for (let i = startPage; i <= endPage; i++) {
//            const isActive = i === currentPage;
//            const buttonClass = isActive ? 'active' : '';
//            paginationContainer.append(`
//                <button class="page-btn ${buttonClass}" data-page="${i}">${i + 1}</button>
//            `);
//        }
//
//        // 다음 그룹 버튼
//        if (endPage < totalPages - 1) {
//            paginationContainer.append('<button class="next-group">다음</button>');
//        }
//
//        // 끝 버튼
//        paginationContainer.append('<button class="last-group">끝</button>');
//
//        // 페이지 버튼 클릭 이벤트
//        $(".page-btn").click(function() {
//            const newPage = $(this).data("page");
//            currentPage = newPage;
//            // 모든 페이지 버튼의 active 클래스 제거
//            $(".page-btn").removeClass('active');
//            // 클릭한 버튼에 active 클래스 추가
//            $(this).addClass('active');
//            loadMarkets(newPage);
//        });
//
//        // 다음 버튼 클릭 이벤트
//        $(".next-group").click(function() {
//            if (currentPage < totalPages - 1) {
//                currentPage++;
//                // 모든 페이지 버튼의 active 클래스 제거
//                $(".page-btn").removeClass('active');
//                // 클릭한 페이지에 해당하는 버튼에 active 클래스 추가
//                $(`.page-btn[data-page="${currentPage}"]`).addClass('active');
//                loadMarkets(currentPage);
//            }
//        });
//
//        // 이전 버튼 클릭 이벤트
//        $(".prev-group").click(function() {
//            if (currentPage > 0) {
//                currentPage--;
//                // 모든 페이지 버튼의 active 클래스 제거
//                $(".page-btn").removeClass('active');
//                // 클릭한 페이지에 해당하는 버튼에 active 클래스 추가
//                $(`.page-btn[data-page="${currentPage}"]`).addClass('active');
//                loadMarkets(currentPage);
//            }
//        });
//
//        // 처음 버튼 클릭 이벤트
//        $(".first-group").click(function() {
//            currentPage = 0;
//            // 모든 페이지 버튼의 active 클래스 제거
//            $(".page-btn").removeClass('active');
//            // 첫 번째 페이지 버튼에 active 클래스 추가
//            $(`.page-btn[data-page="${currentPage}"]`).addClass('active');
//            loadMarkets(currentPage);
//        });
//
//        // 끝 버튼 클릭 이벤트
//        $(".last-group").click(function() {
//            currentPage = totalPages - 1;
//            // 모든 페이지 버튼의 active 클래스 제거
//            $(".page-btn").removeClass('active');
//            // 마지막 페이지 버튼에 active 클래스 추가
//            $(`.page-btn[data-page="${currentPage}"]`).addClass('active');
//            loadMarkets(currentPage);
//        });
//    }
//});



$(document).ready(function() {
    let currentPage = 0;
    const groupSize = 5; // 한 그룹에 보여줄 페이지 수
    let searchQuery = ""; // 검색어 초기화
    let selectedCategory = ""; // 선택된 카테고리 초기화
    let sortBy = "marketName,asc"; // 기본 정렬 기준

    // 페이지가 로드될 때 첫 번째 페이지의 시장 목록 가져오기
    loadMarkets(currentPage, searchQuery, selectedCategory);

    // 초기 카테고리 버튼 생성
    const categories = ["서울", "인천", "경기도", "강원", "충청도", "경상도", "전라도", "제주도"];
    categories.forEach(category => {
        $(".marketList-category").append(`<button class="category-btn" data-category="${category}">${category}</button>`);
    });

    // 전체보기 버튼 클릭 이벤트
    $("#viewAllButton").click(function() {
        selectedCategory = ""; // 카테고리 선택 초기화
        searchQuery = ""; // 검색어 초기화
        $("#searchInput").val('');
        currentPage = 0; // 페이지 초기화
        sortBy = "marketName,asc"; // 정렬 기준 초기화
        loadMarkets(currentPage, searchQuery, selectedCategory);
    });

    // 시장 목록을 가져오는 함수
    function loadMarkets(page, keyword, category) {
        let url;
        const params = new URLSearchParams();

        // 검색어가 있을 경우
        if (keyword) {
            url = "/api/markets/search"
            params.append("keyword", keyword);

        } else if(category) {
            url = "/api/markets/category";
            params.append("category", category);

        } else  {
            url = "/api/markets";
        }

        params.append("page", page);
        params.append("size", 3); // 페이지 당 항목 수
        params.append("sort", sortBy); // 정렬 기준

        // URL과 쿼리 파라미터 조합
        const fullUrl = `${url}?${params.toString()}`;

        $.ajax({
            url: fullUrl,
            type: "GET",
            success: function(data) {
                console.log(data);
                let contentHtml = `<h2>시장 찾기</h2>`;

                // 시장이 없을 경우
                if (data.content.length === 0) {
                    $(".marketList-contents").html(`
                        <h2>시장 찾기</h2>
                        <p>시장이 존재하지 않습니다</p>
                        <button id="viewAllItemsBtn">전체 상점 목록 보기</button>
                    `);
                    // 전체 시장 목록 보기 버튼 클릭 이벤트 핸들러 추가
                    $("#viewAllItemsBtn").click(function() {
                        // 페이지 새로고침
                        location.reload();
                    });
                    return;
                }

                let marketTable = `
                    <table>
                        <thead>
                            <tr>
                                <th>시장 이름</th>
                                <th>주소</th>
                                <th>상세 설명</th>
                                <th>좋아요 수</th>
                               <!-- <th>이미지</th> -->
                            </tr>
                        </thead>
                        <tbody>`;

                data.content.forEach(function(market) {
                    let marketUrl = "/markets/" + market.marketNo;
                    marketTable += `
                        <tr>
                            <td><a href="${marketUrl}" class="market-name">${market.marketName}</a></td>
                            <td>${market.marketAddr}</td>
                            <td>${market.marketDetail}</td>
                            <td>${market.likes}</td>
                            <!--
                            <td>
                                ${market.imageList.length > 0 ? `<img src="${market.imageList[0].imageUrl}" alt="${market.marketName} 이미지">` : '이미지 없음'}
                            </td>
                            -->
                        </tr>`;
                });

                marketTable += `
                        </tbody>
                    </table>`;

                // 전체 내용 설정
                contentHtml += marketTable;
                $(".marketList-contents").html(contentHtml); // marketList-contents에 테이블 추가

                // 페이지네이션 렌더링
                renderPagination(data.totalPages);
            },
            error: function(xhr) {
                $(".marketList-contents").html(`<p>${xhr.responseText}</p>`);
            }
        });
    }

    // 카테고리 버튼 클릭 이벤트
    $(document).on('click', '.category-btn', function() {
        selectedCategory = $(this).data('category');
        currentPage = 0; // 페이지를 0으로 초기화
        searchQuery = ""; // 검색어 초기화
        $("#searchInput").val('');
        sortBy = "marketName,asc";
        loadMarkets(currentPage, searchQuery, selectedCategory);
    });

    // 검색 버튼 클릭 이벤트
    $("#searchButton").click(function() {
        searchQuery = $("#searchInput").val();
        currentPage = 0; // 검색 시 페이지를 0으로 초기화
        loadMarkets(currentPage, searchQuery, selectedCategory);
    });

    // 엔터 키로 검색
    $("#searchInput").keypress(function(e) {
        if (e.which == 13) { // Enter 키
            $("#searchButton").click();
        }
    });

    function renderPagination(totalPages) {
        const paginationContainer = $(".pagination");
        paginationContainer.empty();

        // 현재 페이지를 기준으로 현재 그룹 계산
        const currentGroup = Math.floor(currentPage / groupSize);

        // 처음 버튼
        paginationContainer.append('<button class="first-group">처음</button>');

        // 이전 그룹 버튼
        if (currentGroup > 0) {
            paginationContainer.append('<button class="prev-group">이전</button>');
        }

        // 페이지 버튼
        const startPage = currentGroup * groupSize;
        const endPage = Math.min(startPage + groupSize - 1, totalPages - 1);

        for (let i = startPage; i <= endPage; i++) {
            const isActive = i === currentPage;
            const buttonClass = isActive ? 'active' : '';
            paginationContainer.append(`
                <button class="page-btn ${buttonClass}" data-page="${i}">${i + 1}</button>
            `);
        }

        // 다음 그룹 버튼
        if (endPage < totalPages - 1) {
            paginationContainer.append('<button class="next-group">다음</button>');
        }

        // 끝 버튼
        paginationContainer.append('<button class="last-group">끝</button>');

        // 페이지 버튼 클릭 이벤트
        $(".page-btn").click(function() {
            const newPage = $(this).data("page");
            currentPage = newPage;
            // 모든 페이지 버튼의 active 클래스 제거
            $(".page-btn").removeClass('active');
            // 클릭한 버튼에 active 클래스 추가
            $(this).addClass('active');
            loadMarkets(newPage, searchQuery, selectedCategory);
        });

        // 다음 버튼 클릭 이벤트
        $(".next-group").click(function() {
            if (currentPage < totalPages - 1) {
                currentPage++;
                // 모든 페이지 버튼의 active 클래스 제거
                $(".page-btn").removeClass('active');
                // 클릭한 페이지에 해당하는 버튼에 active 클래스 추가
                $(`.page-btn[data-page="${currentPage}"]`).addClass('active');
                loadMarkets(currentPage, searchQuery, selectedCategory);
            }
        });

        // 이전 버튼 클릭 이벤트
        $(".prev-group").click(function() {
            if (currentPage > 0) {
                currentPage--;
                // 모든 페이지 버튼의 active 클래스 제거
                $(".page-btn").removeClass('active');
                // 클릭한 페이지에 해당하는 버튼에 active 클래스 추가
                $(`.page-btn[data-page="${currentPage}"]`).addClass('active');
                loadMarkets(currentPage, searchQuery, selectedCategory);
            }
        });

        // 처음 버튼 클릭 이벤트
        $(".first-group").click(function() {
            currentPage = 0;
            // 모든 페이지 버튼의 active 클래스 제거
            $(".page-btn").removeClass('active');
            // 첫 번째 페이지 버튼에 active 클래스 추가
            $(`.page-btn[data-page="${currentPage}"]`).addClass('active');
            loadMarkets(currentPage, searchQuery, selectedCategory);
        });

        // 끝 버튼 클릭 이벤트
        $(".last-group").click(function() {
            currentPage = totalPages - 1;
            // 모든 페이지 버튼의 active 클래스 제거
            $(".page-btn").removeClass('active');
            // 마지막 페이지 버튼에 active 클래스 추가
            $(`.page-btn[data-page="${currentPage}"]`).addClass('active');
            loadMarkets(currentPage, searchQuery, selectedCategory);
        });
    }
});

