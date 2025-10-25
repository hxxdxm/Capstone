package com.example.test

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.test.ui.theme.TestTheme
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.datastore.preferences.core.stringSetPreferencesKey

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TestTheme {
                val navController = rememberNavController()
                MainNavGraph(navController)
            }
        }
    }
}

@Composable
fun MainNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "settings"
    ) {
        composable("settings") { SettingsScreen(navController) }
        composable("camera") { CameraScreen() }
        composable("gallery") { GalleryScreen() }
        composable("face_register") { FaceRegisterScreen(navController) }
        composable("face_list") { FaceListScreen(navController) }
    }
}

@Composable
fun SettingsScreen(navController: NavHostController) {
    var selectedTab by remember { mutableStateOf("설정") }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(selectedTab) { newTab ->
                selectedTab = newTab
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                "설정" -> SettingsContent(navController)
                "카메라" -> CameraScreen()
                "갤러리" -> GalleryScreen()
            }
        }
    }
}

@Composable
fun SettingsContent(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(
            text = "Mozik",
            fontSize = 33.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF111111),
            modifier = Modifier.align(Alignment.Start)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(Color.Black)
                .padding(bottom = 16.dp)
        )

        MosiacSection(navController)
    }
}

@Composable
fun MosiacSection(navController: NavHostController) {
    val context = LocalContext.current

    // ✅ 앱 시작 시 저장된 상태 불러오기
    val (savedMosaic, savedFace, savedPlate) = loadMosaicStates(context)

    // ✅ rememberSaveable + 저장된 값으로 초기화
    var mosaicOn by rememberSaveable { mutableStateOf(savedMosaic) }
    var faceOn by rememberSaveable { mutableStateOf(savedFace) }
    var plateOn by rememberSaveable { mutableStateOf(savedPlate) }

    // ✅ 값이 바뀔 때마다 DataStore에 자동 저장
    LaunchedEffect(mosaicOn, faceOn, plateOn) {
        saveMosaicStates(context, mosaicOn, faceOn, plateOn)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
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

        MosaicList(
            isActive = mosaicOn,
            faceOn = faceOn,
            plateOn = plateOn,
            onFaceChange = { faceOn = it },
            onPlateChange = { plateOn = it },
            navController = navController
        )
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
        Text("🖼 갤러리 화면", fontSize = 22.sp)
    }
}

@Composable
fun BottomNavigationBar(selectedTab: String, onTabSelected: (String) -> Unit) {
    val tabs = listOf("설정", "카메라", "갤러리")

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

// ✅ 얼굴 목록 저장 / 불러오기
suspend fun saveFaceList(context: Context, list: Set<String>) {
    context.dataStore.edit { prefs -> prefs[FACE_LIST] = list }
}

fun loadFaceList(context: Context): MutableList<String> = runBlocking {
    val prefs = context.dataStore.data.first()
    prefs[FACE_LIST]?.toMutableList() ?: mutableListOf()
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