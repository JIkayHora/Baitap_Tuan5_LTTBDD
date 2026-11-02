package com.example.btt5_todo_login

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.btt5_todo_login.ui.theme.Btt5_todo_loginTheme
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Btt5_todo_loginTheme {
                Surface( modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background){
                    TodoApp()
                }
            }
        }
    }
}
data class StartScreen(
    val route: String,
    val nextRoute: String,
    val logoRes: Int,
    val appName: String,
    val slogan: String,
    val version: String
)
data class Category(
    val tagName: String,
    val textColor: Color,
    val bgColor: Color
)
data class Onboarding(
    val route: String,
    val nextRoute: String,
    val stepIndex: Int,
    val logoRes: Int,
    val appName: String,
    val skipText: String,
    val title: String,
    val description: String,
    val buttonText: String,
    val checklistStates: List<Boolean> = emptyList(),
    val categories: List<Category> = emptyList(),
    val reminderText: String = ""
)
data class LoginScreenData(
    val route: String,
    val nextRoute: String,
    val logoRes: Int,
    val title: String,
    val description: String,
    val usernamePlaceholder: String,
    val passwordPlaceholder: String,
    val loginButtonText: String,
    val googleButtonText: String,
    val guestButtonText: String,
    val termsText: String
)
class TodoTask(
    val id: String,
    val title: String,
    val category: Category,
    val time: String,
    initialCompleted: Boolean = false
) {
    var isCompleted by mutableStateOf(initialCompleted)
}
data class DailyStat(
    val dayLabel: String,
    val heightRatio: Float,
    val isHighlighted: Boolean = false
)
data class UserProfile(
    val initials: String,
    val fullName: String,
    val email: String,
    val accountBadge: String,
    val completedTasks: Int,
    val pendingTasks: Int,
    val completionRate: String,
    val weeklyStats: List<DailyStat>
)
data class HomeScreenData(
    val route: String,
    val dateText: String,
    val progressTitle: String,
    val progressEncourage: String,
    val todaySectionTitle: String,
    val seeAllText: String
)
class TodoAppRepository {
    val studyCategory = Category(
        "Học tập",
        Color(0xFF254EDB),
        Color(0xFFDCE6FF)
    )
    val lifeCategory = Category(
        "Cuộc sống",
        Color(0xFFD97706),
        Color(0xFFFEF3C7)
    )
    val startData = StartScreen(
        route = "start",
        nextRoute = "step_1",
        logoRes = R.drawable.logo_app,
        appName = "UTH Todo",
        slogan = "Việc học, việc nhà - gọn gàng mỗi ngày",
        version = "Phiên bản 1.0"
    )
    val onboardingList = listOf(
        Onboarding(
            route = "step_1",
            nextRoute = "step_2",
            logoRes = R.drawable.logo_nho,
            stepIndex = 0,
            appName = "UTH Todo",
            skipText = "Bỏ qua",
            title = "Mọi việc, gọn trong một chỗ",
            description = "Ghi lại bài tập, việc nhà, lịch hẹn. Không còn quên việc nào.",
            buttonText = "Tiếp tục",
            checklistStates = listOf(true, false, false)
        ),
        Onboarding(
            route = "step_2",
            nextRoute = "step_3",
            logoRes = R.drawable.logo_nho,
            stepIndex = 1,
            appName = "UTH Todo",
            skipText = "Bỏ qua",
            title = "Chia theo Học tập và Cuộc sống",
            description = "Mỗi việc có nhóm riêng, nhìn là biết hôm nay cần làm gì.",
            buttonText = "Tiếp tục",
            categories = listOf(studyCategory, lifeCategory)
        ),
        Onboarding(
            route = "step_3",
            nextRoute = "login",
            stepIndex = 2,
            logoRes = R.drawable.logo_nho,
            appName = "UTH Todo",
            skipText = "Bỏ qua",
            title = "Nhắc đúng lúc",
            description = "Bật thông báo để UTH Todo nhắc bạn trước hạn chót.",
            buttonText = "Bắt đầu ngay",
            reminderText = "Ôn bài Toán lúc 19:00"
        )
    )
    val loginData = LoginScreenData(
        route = "login",
        nextRoute = "home",
        logoRes = R.drawable.logo_nho,
        title = "Chào mừng đến với\nUTH Todo",
        description = "Đăng nhập để lưu công việc và đồng bộ\ntrên mọi thiết bị của bạn",
        usernamePlaceholder = "Tên đăng nhập hoặc Email",
        passwordPlaceholder = "Mật khẩu",
        loginButtonText = "Đăng nhập",
        googleButtonText = "Tiếp tục với Google",
        guestButtonText = "Dùng thử không cần tài khoản",
        termsText = "Khi tiếp tục, bạn đồng ý với Điều khoản sử dụng và Chính sách quyền riêng tư của UTH Todo"
    )
    val homeData = HomeScreenData(
        route = "home",
        dateText = "Thứ Ba, 20 tháng 10",
        progressTitle = "Tiến độ hôm nay",
        progressEncourage = "Đã hơn nửa chặng, cố lên!",
        todaySectionTitle = "Việc hôm nay",
        seeAllText = "Xem tất cả"
    )
    val tasks = mutableStateListOf(
        TodoTask("1", "Tập thể dục 30 phút", lifeCategory, "06:30", true),
        TodoTask("2", "Nộp bài tập Lập trình di động", studyCategory, "09:00", true),
        TodoTask("3", "Ôn chương 3 - Cấu trúc dữ liệu", studyCategory, "14:00", false),
        TodoTask("4", "Đi chợ mua đồ cho tuần", lifeCategory, "17:30", false)
    )
    val defaultStats = listOf(
        DailyStat("T2", 0.55f, false),
        DailyStat("T3", 0.35f, true),
        DailyStat("T4", 0.08f, false),
        DailyStat("T5", 0.08f, false),
        DailyStat("T6", 0.08f, false),
        DailyStat("T7", 0.08f, false),
        DailyStat("CN", 0.08f, false)
    )
    val googleUserProfile = UserProfile(
        initials = "MA",
        fullName = "Minh Anh",
        email = "minhanh@example.com",
        accountBadge = "✓ Đã liên kết tài khoản Google",
        completedTasks = 2,
        pendingTasks = 4,
        completionRate = "33%",
        weeklyStats = defaultStats
    )
    val guestUserProfile = UserProfile(
        initials = "KH",
        fullName = "Khách",
        email = "Chưa liên kết email",
        accountBadge = "Tài khoản dùng thử (Khách)",
        completedTasks = 2,
        pendingTasks = 4,
        completionRate = "33%",
        weeklyStats = defaultStats
    )
    fun createCustomUserProfile(username: String): UserProfile {
        val cleanName = username.trim()
        val initials = cleanName.take(2).uppercase()
        return UserProfile(
            initials = initials,
            fullName = cleanName,
            email = if (cleanName.contains("@")) cleanName else "$cleanName@ut.edu.vn",
            accountBadge = "✓ Đã đăng nhập bằng Tài khoản",
            completedTasks = 2,
            pendingTasks = 4,
            completionRate = "33%",
            weeklyStats = defaultStats
        )
    }
}
val appData = TodoAppRepository()
@Composable
fun TodoApp() {
    val navControl = rememberNavController()
    val startScreen = appData.startData
    val pages = appData.onboardingList
    var currentUser by remember { mutableStateOf(appData.googleUserProfile) }
    NavHost(navControl, startDestination = startScreen.route) {
        // Màn hình 1: Start
        composable(startScreen.route) {
            StartScreen(
                data = startScreen,
                onNavigate = { route -> navControl.navigate(route) }
            )
        }
        composable(pages[0].route) {
            OnboardingScreen(
                data = pages[0],
                totalSteps = pages.size,
                onNavigate = { route -> navControl.navigate(route) }
            )
        }
        composable(pages[1].route) {
            OnboardingScreen(
                data = pages[1],
                totalSteps = pages.size,
                onNavigate = { route -> navControl.navigate(route) }
            )
        }
        composable(pages[2].route) {
            OnboardingScreen(
                data = pages[2],
                totalSteps = pages.size,
                onNavigate = { route -> navControl.navigate(route) }
            )
        }
        composable(appData.loginData.route) {
            LoginScreen(
                data = appData.loginData,
                onLoginSuccess = { profile ->
                    currentUser = profile
                    navControl.navigate(appData.homeData.route) {
                        popUpTo(appData.loginData.route) { inclusive = true }
                    }
                }
            )
        }
        composable(appData.homeData.route) {
            HomeScreen(
                data = appData.homeData,
                user = currentUser,
                tasks = appData.tasks,
                studyCategory = appData.studyCategory,
                lifeCategory = appData.lifeCategory,
                onNavigate = { route -> navControl.navigate(route) }
            )
        }
        composable("profile") {
            ProfileScreen(
                user = currentUser,
                onBack = { navControl.popBackStack() },
                onLogout = {
                    navControl.navigate(appData.loginData.route) {
                        popUpTo(appData.homeData.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
@Composable
fun StartScreen(
    data: StartScreen,
    onNavigate: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF254EDB))
            .clickable { onNavigate(data.nextRoute) }
            .systemBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Image(
            painter = painterResource(id = data.logoRes),
            contentDescription = "App Logo",
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(22.dp))
        )
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = data.appName,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = data.slogan,
            fontSize = 13.sp,
            color = Color(0xFFDCE6FF),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.weight(1f))
        Row(
            modifier = Modifier
                .width(64.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
        ) {
            Box(modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(Color.White))
            Box(modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(Color(0xFF5C7CFA)))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = data.version,
            fontSize = 11.sp,
            color = Color(0xFFB8C9FF)
        )
    }
}
@Composable
fun OnboardingScreen(
    data: Onboarding,
    totalSteps: Int,
    onNavigate: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .systemBarsPadding()
            .padding(horizontal = 24.dp,
            vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = data.logoRes),
                    contentDescription = "Small Logo",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = data.appName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF254EDB)
                )
            }
            Text(
                text = data.skipText,
                fontSize = 13.sp,
                color = Color.Gray,
                modifier = Modifier.clickable { onNavigate(appData.loginData.route) }
            )
        }
        Spacer(modifier = Modifier.height(36.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFFEEF2FF))
        ) {
            if (data.checklistStates.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
                ) {
                    for (isChecked in data.checklistStates) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White)
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isChecked) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF254EDB)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .border(1.5.dp,
                                        Color(0xFFBDBDBD),
                                        CircleShape)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Box(
                                modifier = Modifier
                                    .width(if (isChecked) 90.dp else 75.dp)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isChecked) Color(0xFF9FA8DA) else Color(0xFFE0E0E0))
                            )
                        }
                    }
                }
            } else if (data.categories.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
                ) {
                    for (cat in data.categories) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .padding(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(cat.bgColor)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = cat.tagName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = cat.textColor
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .width(110.dp)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFE0E0E0))
                            )
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.Notifications,
                            contentDescription = null,
                            tint = Color(0xFF254EDB),
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White)
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = data.reminderText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937)
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = data.title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF111827),
            lineHeight = 28.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = data.description,
            fontSize = 13.sp,
            color = Color(0xFF6B7280),
            lineHeight = 20.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (index in 0 until totalSteps) {
                val isSelected = index == data.stepIndex
                Box(
                    modifier = Modifier
                        .width(if (isSelected) 18.dp else 6.dp)
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Color(0xFF254EDB) else Color(0xFFD1D5DB))
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = { onNavigate(data.nextRoute) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF254EDB))
        ) {
            Text(
                text = data.buttonText,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
@Composable
fun LoginScreen(
    data: LoginScreenData,
    onLoginSuccess: (UserProfile) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var showPhoneDialog by remember { mutableStateOf(false) }
    var showOtpDialog by remember { mutableStateOf(false) }
    var phoneNumber by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var storedVerificationId by remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Image(
            painter = painterResource(id = data.logoRes),
            contentDescription = "Logo",
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(16.dp))
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = data.title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF111827),
            lineHeight = 32.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = data.description,
            fontSize = 13.sp,
            color = Color(0xFF6B7280),
            lineHeight = 20.sp
        )
        Spacer(modifier = Modifier.height(28.dp))
        OutlinedTextField(
            value = username,
            onValueChange = {
                username = it
                errorMessage = ""
            },
            placeholder = { Text(data.usernamePlaceholder,
                fontSize = 14.sp,
                color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Person,
                contentDescription = null,
                tint = Color.Gray) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                errorMessage = ""
            },
            placeholder = { Text(data.passwordPlaceholder,
                fontSize = 14.sp,
                color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Lock,
                contentDescription = null,
                tint = Color.Gray) },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )
        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage,
                color = Color(0xFFD32F2F),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (username.isBlank() || password.isBlank()) {
                    errorMessage = "Đăng nhập lỗi: Vui lòng nhập đầy đủ email và mật khẩu!"
                    Toast.makeText(context, "Đăng nhập thất bại!", Toast.LENGTH_SHORT).show()
                } else {
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(username, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(context, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                                val user = FirebaseAuth.getInstance().currentUser
                                onLoginSuccess(
                                    UserProfile(
                                        initials = user?.email?.take(2)?.uppercase() ?: "MA",
                                        fullName = user?.displayName ?: user?.email?.substringBefore("@") ?: "Người dùng",
                                        email = user?.email ?: "",
                                        accountBadge = "✓ Đã đăng nhập bằng Email",
                                        completedTasks = 2,
                                        pendingTasks = 4,
                                        completionRate = "33%",
                                        weeklyStats = appData.defaultStats
                                    )
                                )
                            } else {
                                errorMessage = "Đăng nhập thất bại: ${task.exception?.message}"
                            }
                        }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF254EDB))
        ) {
            Text(data.loginButtonText,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White)
        }
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedButton(
            onClick = {
                val credentialManager = CredentialManager.create(context)
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId("824639778657-d87ec6e4ci3fqusmf7n97qb4q3ba5q67.apps.googleusercontent.com")
                    .setAutoSelectEnabled(false)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                coroutineScope.launch {
                    try {
                        val result = credentialManager.getCredential(
                            request = request,
                            context = context,
                        )
                        val credential = result.credential

                        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                            val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                            FirebaseAuth.getInstance().signInWithCredential(firebaseCredential)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val user = FirebaseAuth.getInstance().currentUser
                                        Toast.makeText(context, "Đăng nhập Google thành công!", Toast.LENGTH_SHORT).show()
                                        onLoginSuccess(
                                            UserProfile(
                                                initials = user?.displayName?.take(2)?.uppercase() ?: "MA",
                                                fullName = user?.displayName ?: "Minh Anh",
                                                email = user?.email ?: "minhanh@example.com",
                                                accountBadge = "✓ Đã liên kết tài khoản Google",
                                                completedTasks = 2,
                                                pendingTasks = 4,
                                                completionRate = "33%",
                                                weeklyStats = appData.defaultStats
                                            )
                                        )
                                    } else {
                                        Toast.makeText(context, "Đăng nhập Firebase thất bại: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Đăng nhập Google thất bại: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF3F4F6)),
                contentAlignment = Alignment.Center
            ) {
                Text("G", fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    color = Color(0xFF254EDB))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = data.googleButtonText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937)
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        OutlinedButton(
            onClick = {
                showPhoneDialog = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF28A745)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Phone, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Tiếp tục với Số điện thoại",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937)
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "hoặc",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = data.guestButtonText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF254EDB),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    FirebaseAuth.getInstance().signInAnonymously()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(context, "Đang dùng thử với tư cách Khách!", Toast.LENGTH_SHORT).show()
                                onLoginSuccess(appData.guestUserProfile)
                            } else {
                                Toast.makeText(context, "Lỗi đăng nhập khách: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
                .padding(vertical = 6.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = data.termsText,
            fontSize = 11.sp,
            color = Color(0xFF9CA3AF),
            textAlign = TextAlign.Center,
            lineHeight = 16.sp,
            modifier = Modifier.fillMaxWidth()
        )
    }

    if (showPhoneDialog) {
        AlertDialog(
            onDismissRequest = { showPhoneDialog = false },
            title = { Text("Đăng nhập bằng Số điện thoại") },
            text = {
                Column {
                    Text("Nhập số điện thoại của bạn:", fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone
                        ),
                        placeholder = { Text("VD: 0987654321") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        var activityContext: Context = context
                        while (activityContext is ContextWrapper) {
                            if (activityContext is Activity) break
                            activityContext = activityContext.baseContext
                        }
                        val activity = activityContext as? Activity

                        val formattedNumber = phoneNumber.trim().let {
                            when {
                                it.startsWith("+") -> it
                                it.startsWith("0") -> "+84" + it.drop(1)
                                it.startsWith("84") -> "+" + it
                                else -> "+84$it"
                            }
                        }
                        
                        if (activity != null && formattedNumber.isNotBlank()) {
                            val options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
                                .setPhoneNumber(formattedNumber)
                                .setTimeout(60L, TimeUnit.SECONDS)
                                .setActivity(activity)
                                .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                                        FirebaseAuth.getInstance().signInWithCredential(credential)
                                            .addOnCompleteListener { task ->
                                                if (task.isSuccessful) {
                                                    val user = FirebaseAuth.getInstance().currentUser
                                                    showPhoneDialog = false
                                                    showOtpDialog = false
                                                    Toast.makeText(context, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                                                    onLoginSuccess(
                                                        UserProfile(
                                                            initials = user?.phoneNumber?.takeLast(2) ?: "SD",
                                                            fullName = user?.phoneNumber ?: "Người dùng",
                                                            email = "Chưa có email",
                                                            accountBadge = "✓ Đã đăng nhập bằng SĐT",
                                                            completedTasks = 2,
                                                            pendingTasks = 4,
                                                            completionRate = "33%",
                                                            weeklyStats = appData.defaultStats
                                                        )
                                                    )
                                                }
                                            }
                                    }
                                    override fun onVerificationFailed(e: FirebaseException) {
                                        android.util.Log.e("PhoneAuth", "Lỗi xác thực SĐT", e)
                                        Toast.makeText(context, "Lỗi xác thực SĐT: ${e.localizedMessage ?: e.message}", Toast.LENGTH_LONG).show()
                                    }
                                    override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                                        storedVerificationId = verificationId
                                        showPhoneDialog = false
                                        showOtpDialog = true
                                        Toast.makeText(context, "Đã gửi mã xác nhận!", Toast.LENGTH_SHORT).show()
                                    }
                                }).build()
                            PhoneAuthProvider.verifyPhoneNumber(options)
                        } else {
                            Toast.makeText(context, "Vui lòng nhập số điện thoại hợp lệ", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) { Text("Gửi mã") }
            },
            dismissButton = {
                TextButton(onClick = { showPhoneDialog = false }) { Text("Hủy") }
            }
        )
    }

    if (showOtpDialog) {
        AlertDialog(
            onDismissRequest = { showOtpDialog = false },
            title = { Text("Xác thực OTP") },
            text = {
                Column {
                    Text("Nhập mã OTP gồm 6 chữ số:", fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = otpCode,
                        onValueChange = { otpCode = it },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (otpCode.isNotBlank() && storedVerificationId.isNotBlank()) {
                            val credential = PhoneAuthProvider.getCredential(storedVerificationId, otpCode)
                            FirebaseAuth.getInstance().signInWithCredential(credential)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val user = FirebaseAuth.getInstance().currentUser
                                        showOtpDialog = false
                                        Toast.makeText(context, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                                        onLoginSuccess(
                                            UserProfile(
                                                initials = user?.phoneNumber?.takeLast(2) ?: "SD",
                                                fullName = user?.phoneNumber ?: "Người dùng",
                                                email = "Chưa có email",
                                                accountBadge = "✓ Đã đăng nhập bằng SĐT",
                                                completedTasks = 2,
                                                pendingTasks = 4,
                                                completionRate = "33%",
                                                weeklyStats = appData.defaultStats
                                            )
                                        )
                                    } else {
                                        Toast.makeText(context, "Sai mã OTP!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        }
                    }
                ) { Text("Xác nhận") }
            },
            dismissButton = {
                TextButton(onClick = { showOtpDialog = false }) { Text("Hủy") }
            }
        )
    }
}
@Composable
fun HomeScreen(
    data: HomeScreenData,
    user: UserProfile,
    tasks: List<TodoTask>,
    studyCategory: Category,
    lifeCategory: Category,
    onNavigate: (String) -> Unit
) {
    val completedCount = tasks.count { it.isCompleted }
    val totalCount = tasks.size
    val progressPercent = if (totalCount > 0) (completedCount * 100) / totalCount else 0

    val studyRemaining = tasks.count { it.category.tagName == studyCategory.tagName && !it.isCompleted }
    val lifeRemaining = tasks.count { it.category.tagName == lifeCategory.tagName && !it.isCompleted }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onNavigate("profile") }
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFDCE6FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.initials,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF254EDB)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = data.dateText,
                            fontSize = 11.sp,
                            color = Color.Gray)
                        Text(
                            text = "Chào ${user.fullName}!",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827)
                        )
                    }
                }
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Notifications",
                        tint = Color(0xFF374151)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF254EDB))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = data.progressTitle,
                            fontSize = 12.sp,
                            color = Color(0xFFDCE6FF)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$completedCount/$totalCount việc",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = data.progressEncourage,
                            fontSize = 12.sp,
                            color = Color(0xFFDCE6FF)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .border(4.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$progressPercent%",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(studyCategory.bgColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = studyCategory.textColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(studyCategory.tagName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp)
                        Text("$studyRemaining việc còn lại",
                            fontSize = 11.sp,
                            color = Color.Gray)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(lifeCategory.bgColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Home,
                                contentDescription = null,
                                tint = lifeCategory.textColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(lifeCategory.tagName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp)
                        Text("$lifeRemaining việc còn lại",
                            fontSize = 11.sp,
                            color = Color.Gray)
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = data.todaySectionTitle,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
                Text(
                    text = data.seeAllText,
                    fontSize = 12.sp,
                    color = Color(0xFF254EDB),
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(tasks) { task ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { task.isCompleted = !task.isCompleted },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (task.isCompleted) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF254EDB)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .border(1.5.dp, Color(0xFFD1D5DB), CircleShape)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = task.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (task.isCompleted) Color.Gray else Color(0xFF111827),
                                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(task.category.bgColor)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = task.category.tagName,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = task.category.textColor
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = task.time,
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 1.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(Color.White),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Home,
                    contentDescription = null,
                    tint = Color(0xFF254EDB))
                Text("Trang chủ",
                    fontSize = 10.sp,
                    color = Color(0xFF254EDB),
                    fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.List,
                    contentDescription = null,
                    tint = Color.Gray)
                Text("Danh sách",
                    fontSize = 10.sp,
                    color = Color.Gray)
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF254EDB)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add,
                    contentDescription = "Add",
                    tint = Color.White)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Info,
                    contentDescription = null,
                    tint = Color.Gray)
                Text("Thống kê",
                    fontSize = 10.sp,
                    color = Color.Gray)
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onNavigate("profile") }
            ) {
                Icon(Icons.Default.Settings,
                    contentDescription = null,
                    tint = Color.Gray)
                Text("Cài đặt",
                    fontSize = 10.sp,
                    color = Color.Gray)
            }
        }
    }
}
@Composable
fun ProfileScreen(
    user: UserProfile,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
            .systemBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .clickable { onBack() }
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowLeft,
                    contentDescription = "Back",
                    tint = Color(0xFF254EDB)
                )
                Text(
                    text = "Cài đặt",
                    fontSize = 14.sp,
                    color = Color(0xFF254EDB),
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                text = "Hồ sơ",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827),
                modifier = Modifier.align(Alignment.Center)
            )
        }
        Spacer(modifier = Modifier.height(28.dp))
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(Color(0xFFDCE6FF)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = user.initials,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF254EDB)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = user.fullName,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF111827)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = user.email,
            fontSize = 13.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFDCFCE7))
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(
                text = user.accountBadge,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF15803D)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("${user.completedTasks}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold)
                    Text("Việc đã xong",
                        fontSize = 11.sp,
                        color = Color.Gray)
                }
            }
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("${user.pendingTasks}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold)
                    Text("Đang chờ",
                        fontSize = 11.sp,
                        color = Color.Gray)
                }
            }
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(user.completionRate,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold)
                    Text("Hoàn thành",
                        fontSize = 11.sp,
                        color = Color.Gray)
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Tuần này",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp)
                    Text("Việc xong mỗi ngày",
                        fontSize = 11.sp,
                        color = Color.Gray)
                }
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.Bottom
                ) {
                    for (stat in user.weeklyStats) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(16.dp)
                                    .fillMaxHeight(stat.heightRatio)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        if (stat.isHighlighted) Color(0xFF254EDB)
                                        else if (stat.heightRatio > 0.2f) Color(0xFF93C5FD)
                                        else Color(0xFFE5E7EB)
                                    )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = stat.dayLabel,
                                fontSize = 10.sp,
                                fontWeight = if (stat.isHighlighted) FontWeight.Bold else FontWeight.Normal,
                                color = if (stat.isHighlighted) Color(0xFF254EDB) else Color.Gray
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = {
                Toast.makeText(context, "Đã đăng xuất!", Toast.LENGTH_SHORT).show()
                onLogout()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
        ) {
            Text(
                text = "[→ Đăng xuất",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFDC2626)
            )
        }
    }
}
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Btt5_todo_loginTheme {
        TodoApp()
    }
}