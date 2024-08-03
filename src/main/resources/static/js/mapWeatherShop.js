function showShopInfo(address, shopName) {
    // address가 없을 경우 기본 주소 설정
    if (!address) {
        address = '서울특별시 중구 세종대로 110 서울특별시청';
    }

    // shopName 없을 경우 기본 이름 설정
    if (!shopName) {
        shopName = '서울시청';
    }
    console.log(shopName + ", " + address);

    // 네이버 지도 API로 주소를 검색하여 지도에 표시
    naver.maps.Service.geocode({
        query: address
    }, function(status, response) {
        if (status !== naver.maps.Service.Status.OK) {
            return alert('지도를 표시하는 중 오류가 발생했습니다.');
        }

        var result = response.v2;
        var items = result.addresses;

        if (items.length > 0) {
            var firstItem = items[0];

            var x = parseFloat(firstItem.x);
            var y = parseFloat(firstItem.y);

            var jibunAddress = firstItem.jibunAddress;
            var roadAddress = firstItem.roadAddress;

            // 콘솔에 query 값 출력
            console.log("query:", address);

            // 지도 옵션 설정
            var mapOptions = {
                center: new naver.maps.LatLng(y, x),
                zoom: 15
            };

            // 지도 생성
            var map = new naver.maps.Map('map', mapOptions);

            // 마커 생성 및 정보창 설정
            var marker = new naver.maps.Marker({
                position: new naver.maps.LatLng(y, x),
                map: map
            });

            var infoWindow = new naver.maps.InfoWindow({
                content: [
                    '<div style="padding:10px;min-width:200px;line-height:150%;">',
                    '<h3>' + shopName + '</h3>',
                    '<h4>지번 주소</h4>',
                    '<p>' + jibunAddress + '</p>',
                    '<h4>도로명 주소</h4>',
                    '<p>' + roadAddress + '</p>',
                    '</div>'
                ].join('')
            });

            // 마커 클릭 시 정보창 열기
            naver.maps.Event.addListener(marker, "click", function() {
                if (infoWindow.getMap()) {
                    infoWindow.close();
                } else {
                    infoWindow.open(map, marker);
                }
            });

            // 마커 외 클릭 시 정보창 닫기
            naver.maps.Event.addListener(map, 'click', function() {
                infoWindow.close();
            });

            // 자동으로 정보창 열기
            infoWindow.open(map, marker);

            /* 여기서부터 날씨 API */
            // OpenWeather API로 날씨 정보 가져오기
            var apiKey = '49a8252265aef937b366a8506d1fec4f';
            var weatherUrl = 'https://api.openweathermap.org/data/2.5/weather?lat=' + y + '&lon=' + x + '&appid=' + apiKey + '&units=metric';

            fetch(weatherUrl)
                .then(response => response.json())
                .then(data => {
                    console.log(data);

                    var currentTemp = data.main.temp;
                    var feelsLike = data.main.feels_like;
                    var minTemp = data.main.temp_min;
                    var maxTemp = data.main.temp_max;
                    var humidity = data.main.humidity;
                    var weatherDescription = data.weather[0].description;
                    var weatherCode = data.weather[0].icon;
                    console.log(weatherDescription);

                    // 날씨 정보를 HTML에 출력
                    document.getElementById('current-temp').textContent = '현재 온도: ' + currentTemp + '°C';
                    document.getElementById('feels-like').textContent = '체감 온도: ' + feelsLike + '°C';
                    document.getElementById('min-temp').textContent = '최저 온도: ' + minTemp + '°C';
                    document.getElementById('max-temp').textContent = '최고 온도: ' + maxTemp + '°C';
                    document.getElementById('humidity').textContent = '습도: ' + humidity + '%';

                    // 날씨 상태에 따른 설명을 한국어로 변환
                    var weatherDescriptionKorean = getKoreanWeatherDescription(weatherDescription);
                    document.getElementById('weather-description').textContent = '날씨: ' + weatherDescriptionKorean;

                    // 날씨 상태에 따른 이미지 매핑
                    const imageUrl = "https://openweathermap.org/img/wn/" + weatherCode + "@2x.png";
                    const weatherImage = `<img src="${imageUrl}" alt="${weatherDescription}">`;

                    // 이미지를 화면에 표시
                    const weatherImageContainer = document.getElementById('weather-image-container');
                    weatherImageContainer.innerHTML = weatherImage;

                    // 날씨 상태를 한국어로 변환하는 함수
                    function getKoreanWeatherDescription(weatherDescription) {
                        switch (weatherDescription) {
                            case 'thunderstorm with light rain':
                                return '약한 천둥번개와 비';
                            case 'thunderstorm with rain':
                                return '천둥번개와 비';
                            case 'thunderstorm with heavy rain':
                                return '강한 천둥번개와 비';
                            case 'light thunderstorm':
                                return '약한 천둥번개';
                            case 'thunderstorm':
                                return '천둥번개';
                            case 'heavy thunderstorm':
                                return '강한 천둥번개';
                            case 'ragged thunderstorm':
                                return '불규칙한 천둥번개';
                            case 'thunderstorm with light drizzle':
                                return '약한 천둥번개와 가랑비';
                            case 'thunderstorm with drizzle':
                                return '천둥번개와 가랑비';
                            case 'thunderstorm with heavy drizzle':
                                return '강한 천둥번개와 가랑비';
                            case 'clear sky':
                                return '맑음';
                            case 'few clouds':
                                return '구름 조금';
                            case 'scattered clouds':
                                return '흐린 후 간헐적 구름';
                            case 'overcast clouds':
                                return '흐리고 구름';
                            case 'broken clouds':
                                return '부서진 구름';
                            case 'shower rain':
                                return '소나기';
                            case 'rain':
                                return '비';
                            case 'light rain':
                                return '약한 비';
                            case 'moderate rain':
                                return '중간 비';
                            case 'heavy intensity rain':
                                return '매우 강한 비';
                            case 'very heavy rain':
                                return '매우 많은 비';
                            case 'drizzle':
                                return '이슬비';
                            case 'light intensity drizzle rain':
                                return '약한 이슬비';
                            case 'drizzle rain':
                                return '이슬비';
                            case 'heavy intensity drizzle rain':
                                return '강한 이슬비';
                            case 'shower rain and drizzle':
                                return '소나기와 이슬비';
                            case 'shower drizzle':
                                return '소나기 이슬비';
                            case 'light snow':
                                return '약한 눈';
                            case 'snow':
                                return '눈';
                            case 'heavy snow':
                                return '많은 눈';
                            case 'sleet':
                                return '진눈깨비';
                            case 'shower sleet':
                                return '소나기 진눈깨비';
                            case 'light rain and snow':
                                return '약한 비와 눈';
                            case 'rain and snow':
                                return '비와 눈';
                            case 'light shower snow':
                                return '약한 소나기 눈';
                            case 'shower snow':
                                return '소나기 눈';
                            case 'heavy shower snow':
                                return '강한 소나기 눈';
                            case 'mist':
                                return '안개';
                            case 'smoke':
                                return '연기';
                            case 'haze':
                                return '연무';
                            case 'sand/ dust whirls':
                                return '모래/먼지 회오리';
                            case 'fog':
                                return '안개';
                            case 'sand':
                                return '모래';
                            case 'dust':
                                return '먼지';
                            case 'volcanic ash':
                                return '화산재';
                            case 'squalls':
                                return '돌풍';
                            case 'tornado':
                                return '토네이도';
                            default:
                                return weatherDescription;
                        }
                    }
                })
                .catch(error => {
                    console.error('날씨 정보를 가져오는 중 오류가 발생했습니다.', error);
                    alert('날씨 정보를 가져오는 중 오류가 발생했습니다.');
                });
        } else {
            console.log('주소에 대한 검색 결과가 없습니다');
        }
    });
}