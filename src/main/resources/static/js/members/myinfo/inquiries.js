$(document).ready(function() {
    let currentPage = 0; // 초기 페이지 설정
    const groupSize = 5; // 한 그룹에 보여줄 페이지 수

    // 페이지가 로드될 때 첫 번째 페이지의 문의사항 가져오기
    loadInquiries(currentPage);

    // 내가 작성한 문의사항을 가져오는 함수
    function loadInquiries(page) {
        $.ajax({
            url: "/api/inquiries/m?page=" + page + "&size=3&sort=createTime,desc",
            type: "GET",
            success: function(data) {
                console.log(data);

                // 문의사항 목록 제목을 유지
                let contentHtml = `<h2>문의사항 목록</h2>`;

                // 문의사항이 없을 경우
                if (data.content.length === 0) {
                    $(".inquiryList-contents").html(`
                        <h2>내 문의사항 목록</h2>
                        <p>문의사항이 없습니다</p>
                    `);
                    $(".pagination").remove(); // 페이지네이션이 있으면 제거
                    return; // 함수 실행 종료
                }

                // 문의사항 테이블 생성
                let inquiryTable = `
                    <table>
                        <thead>
                            <tr>
                                <th>문의 제목</th>
                                <th>작성자</th>
                                <th>작성일</th>
                                <th>답변상태</th>
                                <th>삭제</th>
                            </tr>
                        </thead>
                        <tbody>`;

                // 각 문의사항을 테이블에 추가
                data.content.forEach(function(inquiry) {
                    let inquiryUrl = "/myinfo/inquiry/" + inquiry.inquiryNo;
                    inquiryTable += `
                        <tr>
                            <td><a href="${inquiryUrl}" class=inquiry-title">${inquiry.inquiryTitle}</a></td>
                            <td>${inquiry.inquiryWriter}</td>
                            <td>${inquiry.createTime}</td>
                            <td>${inquiry.inquiryState}</td>
                            <td>
                                <button class="delete-inquiry" data-inquiry-no="${inquiry.inquiryNo}">삭제</button>
                            </td>
                        </tr>`;
                });

                inquiryTable += `
                        </tbody>
                    </table>`;

                // 전체 내용 설정
                contentHtml += inquiryTable;
                $(".inquiryList-contents").html(contentHtml); // 문의사항 목록 출력

                // 삭제 버튼 클릭 이벤트 핸들러
                $(".delete-inquiry").on("click", function() {
                    const inquiryNo = $(this).data("inquiry-no");
                    if (confirm("정말로 이 문의를 삭제하시겠습니까?")) {
                        $.ajax({
                            url: "/api/inquiries/" + inquiryNo,
                            type: "DELETE",
                            success: function(response) {
                                alert(response.message);
                                // 삭제 후 현재 페이지 다시 로드
                                loadInquiries(currentPage);
                            },
                            error: function(xhr) {
                                alert('삭제 실패: ' + xhr.responseText);
                            }
                        });
                    }
                });

                // 페이지네이션 컨트롤 추가
                renderPagination(data.totalPages);
            },
            error: function(xhr) {
                $(".inquiryList-contents").html(`<p>${xhr.responseText}</p>`);
            }
        });
    }

    // 페이지네이션을 생성하는 함수
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
            loadInquiries(newPage);
        });

        // 다음 버튼 클릭 이벤트
        $(".next-group").click(function() {
            if (currentPage < totalPages - 1) {
                currentPage++;
                // 모든 페이지 버튼의 active 클래스 제거
                $(".page-btn").removeClass('active');
                // 클릭한 페이지에 해당하는 버튼에 active 클래스 추가
                $(`.page-btn[data-page="${currentPage}"]`).addClass('active');
                loadInquiries(currentPage);
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
                loadInquiries(currentPage);
            }
        });

        // 처음 버튼 클릭 이벤트
        $(".first-group").click(function() {
            currentPage = 0;
            // 모든 페이지 버튼의 active 클래스 제거
            $(".page-btn").removeClass('active');
            // 첫 번째 페이지 버튼에 active 클래스 추가
            $(`.page-btn[data-page="${currentPage}"]`).addClass('active');
            loadInquiries(currentPage);
        });

        // 끝 버튼 클릭 이벤트
        $(".last-group").click(function() {
            currentPage = totalPages - 1;
            // 모든 페이지 버튼의 active 클래스 제거
            $(".page-btn").removeClass('active');
            // 마지막 페이지 버튼에 active 클래스 추가
            $(`.page-btn[data-page="${currentPage}"]`).addClass('active');
            loadInquiries(currentPage);
        });
    }
});
