package com.example.test

// Compose 기본
import com.example.test.ui.auth.LoginScreen
import com.example.test.ui.auth.SignupScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Navigation
import androidx.navigation.NavHostController
import androidx.navigation.compose.*

// DataStore
import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

// Theme
import com.example.test.ui.theme.TestTheme
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.test.network.AuthApi

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TestTheme {
                val navController = rememberNavController()

                // ✅ Retrofit 객체 생성
                val retrofit = Retrofit.Builder()
                    .baseUrl("http://10.0.2.2:8080/") // 에뮬레이터에서 localhost 접속용
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                val api = retrofit.create(AuthApi::class.java)

                // ✅ NavGraph에 실제 api 전달
                MainNavGraph(navController, api)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavHostController) {
    var selectedTab by remember { mutableStateOf("영상처리기") }
    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            // 🟦 상단 Mozik + ⋮ 점 3개
            TopAppBar(
                title = {
                    Text(
                        text = "Mozik",
                        fontSize = 33.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                },
                actions = {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Text("⋮", fontSize = 33.sp, color = Color.Black)
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("설정") },
                                onClick = {
                                    menuExpanded = false
                                    navController.navigate("settings")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("앱 정보") },
                                onClick = { menuExpanded = false }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },

        bottomBar = {
            BottomNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = { newTab -> selectedTab = newTab }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                "영상처리기" -> VideoProcessorScreen(navController)
                "카메라" -> CameraScreen()
                "갤러리" -> GalleryScreen()
            }
        }
    }
}

@Composable
fun MainNavGraph(navController: NavHostController, api: AuthApi) {
    NavHost(navController = navController, startDestination = "login") {

        // 🔹 로그인 / 회원가입 추가
        composable("login") { LoginScreen(navController, api) }
        composable("signup") { SignupScreen(navController, api) }

        // 🔹 기존 화면 유지
        composable("video_processor") { VideoProcessorScreen(navController) }
        composable("settings") { SettingsContent(navController) }
        composable("camera") { CameraScreen() }
        composable("gallery") { GalleryScreen() }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoProcessorScreen(navController: NavHostController) {
    val sheetState = rememberBottomSheetScaffoldState()

    var blurSize by remember { mutableStateOf(2f) }
    var blurLevel by remember { mutableStateOf(20f) }

    BottomSheetScaffold(
        scaffoldState = sheetState,
        sheetPeekHeight = 100.dp,
        sheetContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(20.dp)
            ) {
                Box(
                    Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .background(Color.LightGray, shape = MaterialTheme.shapes.small)
                        .align(Alignment.CenterHorizontally)
                )
                Spacer(Modifier.height(12.dp))

                Text("블러 설정", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(16.dp))

                Text("블러 크기: ${blurSize.toInt()}")
                Slider(value = blurSize, onValueChange = { blurSize = it }, valueRange = 0f..10f)

                Text("블러 강도: ${blurLevel.toInt()}")
                Slider(value = blurLevel, onValueChange = { blurLevel = it }, valueRange = 0f..100f)
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color(0xFFEFEFEF)),
            contentAlignment = Alignment.Center
        ) {
            Text("🎞 영상 선택하기", fontSize = 22.sp, color = Color.Gray)
        }
    }
}

// ⚪ 영상처리기 탭 임시 비워둔 화면
@Composable
fun EmptyVideoScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "영상처리기 기능 준비 중...",
            fontSize = 18.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun SettingsContent(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        // 🔹 상단 타이틀 줄 (< + Mozik)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // 뒤로가기 <
            Text(
                text = "<",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier
                    .clickable {
                        navController.navigate("video_processor")
                    } // 뒤로가기
                    .padding(end = 12.dp)
            )

            // 제목 Mozik
            Text(
                text = "Mozik",
                fontSize = 33.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF111111)
            )
        }

        // 🔹 검은 줄 구분선
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(Color.Black)
                .padding(bottom = 16.dp)
        )

        // 🔹 모자이크 / 블러 섹션
        MosiacSection(navController)
    }
}

@Composable
fun MosiacSection(navController: NavHostController) {
    val context = LocalContext.current

    // 저장된 상태 불러오기
    val (savedMosaic, savedFace, savedPlate) = loadMosaicStates(context)

    var mosaicOn by rememberSaveable { mutableStateOf(savedMosaic) }
    var faceOn by rememberSaveable { mutableStateOf(savedFace) }
    var plateOn by rememberSaveable { mutableStateOf(savedPlate) }
    var blurValue by rememberSaveable { mutableStateOf(30f) } // ✅ 블러 강도 (0~100)

    // 상태 저장
    LaunchedEffect(mosaicOn, faceOn, plateOn, blurValue) {
        saveMosaicStates(context, mosaicOn, faceOn, plateOn)
        saveBlurValue(context, blurValue)
    }

    Column(modifier = Modifier.fillMaxWidth()) {

        // 모자이크 스위치
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "모자이크",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF333333)
            )

            Switch(
                checked = mosaicOn,
                onCheckedChange = { mosaicOn = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF0099FF),
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color.LightGray
                )
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.Gray)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // ✅ 모자이크가 켜졌을 때만 하위 항목 표시
        if (mosaicOn) {
            MosaicList(
                isActive = mosaicOn,
                faceOn = faceOn,
                plateOn = plateOn,
                onFaceChange = { faceOn = it },
                onPlateChange = { plateOn = it },
                navController = navController
            )

            // ✅ 블러 설정 추가
            BlurSetting(blurValue = blurValue, onBlurChange = { blurValue = it })
        }
    }
}

@Composable
fun MosaicList(
    isActive: Boolean,
    faceOn: Boolean,
    plateOn: Boolean,
    onFaceChange: (Boolean) -> Unit,
    onPlateChange: (Boolean) -> Unit,
    navController: NavHostController
) {
    val textColor = if (isActive) Color.Black else Color(0xFFAAAAAA)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
    ) {
        MosaicItemFace(
            title = "얼굴",
            checked = faceOn,
            onToggle = onFaceChange,
            isActive = isActive,
            textColor = textColor,
            navController = navController
        )
        MosaicItem(
            title = "번호판",
            checked = plateOn,
            onToggle = onPlateChange,
            isActive = isActive,
            textColor = textColor
        )
    }
}

@Composable
fun MosaicItemFace(
    title: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit,
    isActive: Boolean,
    textColor: Color,
    navController: NavHostController
) {
    val context = LocalContext.current
    var faceCount by remember { mutableIntStateOf(loadFaceCount(context)) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
            Switch(
                checked = checked,
                enabled = isActive,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF66BB6A),
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color.LightGray
                )
            )
        }

        if (checked) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp, top = 4.dp, bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // "얼굴 등록하기" 텍스트
                Text(
                    text = "얼굴 등록하기",
                    fontSize = 18.sp,
                    color = textColor,
                    modifier = Modifier.clickable(enabled = isActive) {
                        navController.navigate("face_register")
                    }
                )

                // ✅ 등록된 얼굴 버튼 (1개 이상일 때만 클릭 가능)
                Text(
                    text = "등록된 얼굴 (${faceCount}개)",
                    fontSize = 18.sp,
                    color = if (faceCount > 0) Color(0xFF0099FF) else Color(0xFFCCCCCC),
                    modifier = if (faceCount > 0) {
                        Modifier.clickable {
                            navController.navigate("face_list") // ✅ 목록 페이지로 이동
                        }
                    } else Modifier
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.LightGray)
            )
        }
    }
}

@Composable
fun MosaicItem(
    title: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit,
    isActive: Boolean,
    textColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
        Switch(
            checked = checked,
            enabled = isActive,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF66BB6A),
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color.LightGray
            )
        )
    }
}

@Composable
fun FaceRegisterScreen(navController: NavHostController) {
    val context = LocalContext.current
    var faceCount by remember { mutableIntStateOf(loadFaceCount(context)) }
    val fakeFaces = remember { mutableStateListOf<String>() } // 임의로 목록 저장

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        // 상단 바
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            // 뒤로가기 꺾쇠
            Text(
                text = "<",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier
                    .clickable { navController.popBackStack() } // 뒤로가기
                    .padding(end = 12.dp)
            )
            Text(
                text = "얼굴 등록하기",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        // 등록된 임시 목록
        Text(
            text = "등록된 얼굴 목록 (${fakeFaces.size}개)",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        fakeFaces.forEachIndexed { index, name ->
            Text(
                text = "${index + 1}. $name",
                fontSize = 18.sp,
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 목록 추가 버튼
        Button(
            onClick = {
                val newName = "얼굴 ${fakeFaces.size + 1}"
                fakeFaces.add(newName)

                // 등록된 얼굴 개수 증가 후 DataStore 저장
                faceCount++
                CoroutineScope(Dispatchers.IO).launch {
                    saveFaceCount(context, faceCount)
                }
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("목록 추가하기 (+1)")
        }
    }
}

@Composable
fun CameraScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("📷 카메라 화면", fontSize = 22.sp)
    }
}

@Composable
fun GalleryScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("🖼 갤러리 목록", fontSize = 22.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(navController: NavHostController) {
    var expanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Text(
                "Mozik 영상처리기",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        },
        actions = {
            Box {
                IconButton(onClick = { expanded = true }) {
                    Text("⋮", fontSize = 22.sp, color = Color.Black)
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("설정") },
                        onClick = {
                            expanded = false
                            navController.navigate("settings")
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("앱 정보") },
                        onClick = { expanded = false }
                    )
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    )
}

@Composable
fun BottomNavigationBar(selectedTab: String, onTabSelected: (String) -> Unit) {
    val tabs = listOf("영상처리기", "카메라", "갤러리") // ✅ 수정

    NavigationBar(containerColor = Color.White) {
        tabs.forEach { tab ->
            val isSelected = tab == selectedTab
            val color = if (isSelected) Color(0xFF0099FF) else Color.Gray

            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {},
                label = {
                    Text(
                        text = tab,
                        fontSize = 16.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = color
                    )
                }
            )
        }
    }
}

// DataStore 인스턴스 생성
val Context.dataStore by preferencesDataStore(name = "mosaic_prefs")

// 저장할 key 정의
private val MOSAIC_ON = booleanPreferencesKey("mosaic_on")
private val FACE_ON = booleanPreferencesKey("face_on")
private val PLATE_ON = booleanPreferencesKey("plate_on")
private val FACE_COUNT = intPreferencesKey("face_count")
private val FACE_LIST = stringSetPreferencesKey("face_list")
private val BLUR_VALUE = floatPreferencesKey("blur_value")

// 상태 저장
suspend fun saveMosaicStates(context: Context, mosaicOn: Boolean, faceOn: Boolean, plateOn: Boolean) {
    context.dataStore.edit { prefs ->
        prefs[MOSAIC_ON] = mosaicOn
        prefs[FACE_ON] = faceOn
        prefs[PLATE_ON] = plateOn
    }
}

fun loadMosaicStates(context: Context): Triple<Boolean, Boolean, Boolean> = runBlocking {
    val prefs = context.dataStore.data.first()
    Triple(
        prefs[MOSAIC_ON] ?: false,
        prefs[FACE_ON] ?: false,
        prefs[PLATE_ON] ?: false
    )
}

// 얼굴 등록 개수
suspend fun saveFaceCount(context: Context, count: Int) {
    context.dataStore.edit { prefs -> prefs[FACE_COUNT] = count }
}
fun loadFaceCount(context: Context): Int = runBlocking {
    val prefs = context.dataStore.data.first()
    prefs[FACE_COUNT] ?: 0
}
// 얼굴 목록 저장 / 불러오기
suspend fun saveFaceList(context: Context, list: Set<String>) {
    context.dataStore.edit { prefs -> prefs[FACE_LIST] = list }
}

fun loadFaceList(context: Context): MutableList<String> = runBlocking {
    val prefs = context.dataStore.data.first()
    prefs[FACE_LIST]?.toMutableList() ?: mutableListOf()
}

//블러 값 저장
suspend fun saveBlurValue(context: Context, value: Float) {
    context.dataStore.edit { prefs -> prefs[BLUR_VALUE] = value }
}

fun loadBlurValue(context: Context): Float = runBlocking {
    val prefs = context.dataStore.data.first()
    prefs[BLUR_VALUE] ?: 30f
}
@Composable
fun FaceListScreen(navController: NavHostController) {
    val context = LocalContext.current
    var faceList by remember { mutableStateOf(loadFaceList(context)) }
    var faceCount by remember { mutableIntStateOf(loadFaceCount(context)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        // 상단 제목줄
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text(
                text = "<",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier
                    .clickable { navController.popBackStack() }
                    .padding(end = 12.dp)
            )
            Text(
                text = "등록된 얼굴 목록",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        // 등록된 목록 표시
        if (faceList.isEmpty()) {
            Text(
                text = "등록된 얼굴이 없습니다.",
                fontSize = 18.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 10.dp)
            )
        } else {
            faceList.forEachIndexed { index, name ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${index + 1}. $name", fontSize = 18.sp)
                    Text(
                        text = "삭제",
                        color = Color.Red,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            faceList.remove(name)
                            faceCount = faceList.size
                            CoroutineScope(Dispatchers.IO).launch {
                                saveFaceList(context, faceList.toSet())
                                saveFaceCount(context, faceCount)
                            }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 임의 등록 버튼
        Button(
            onClick = {
                val newName = "얼굴 ${faceList.size + 1}"
                faceList.add(newName)
                faceCount = faceList.size
                CoroutineScope(Dispatchers.IO).launch {
                    saveFaceList(context, faceList.toSet())
                    saveFaceCount(context, faceCount)
                }
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("임의 등록 추가하기 (+1)")
        }
    }
}
@Composable
fun BlurSetting(blurValue: Float, onBlurChange: (Float) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = "블러 강도",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF333333)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Slider(
            value = blurValue,
            onValueChange = onBlurChange,
            valueRange = 0f..100f,
            steps = 9, // 0~100 구간을 10단계 정도로
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF0099FF),
                activeTrackColor = Color(0xFF0099FF)
            )
        )

        Text(
            text = "${blurValue.toInt()}",
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier.align(Alignment.End)
        )
    }
}
