package com.jlss.smartDairy.navigation


import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.jlss.smartDairy.data.model.Entry
import com.jlss.smartDairy.screen.EntryListScreen
import com.jlss.smartDairy.screen.HomeScreen
import com.jlss.smartDairy.screen.EntryScreen
import com.jlss.smartDairy.screen.MemberScreen
import com.jlss.smartDairy.viewmodel.SharedViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    navController: NavController,
    sharedVm: SharedViewModel
) {
    var selectedTab by remember { mutableStateOf(0) }
    var selectedEntryList by remember { mutableStateOf<List<Entry>?>(null) }

    var context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Smart Dairy") },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.ProfileScreen.route) }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Profile")
                    }
                }
            )

        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Edit, contentDescription = "add") },
                    label = { Text("Add") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.List, contentDescription = "list") },
                    label = { Text("All") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Person, contentDescription = "members") },
                    label = { Text("Members") }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> HomeScreen()
                1 -> EntryScreen(onSaved = {
                    // Example: show a Toast or navigate
                    Toast.makeText(context, "Entry Saved!", Toast.LENGTH_SHORT).show()
                })
                2 -> EntryListScreen(
                    navController = navController,
                    sharedVm = sharedVm
                )
                3 -> MemberScreen(
                    navController = navController
                )

            }
        }
    }
}
/*
@Composable
fun MemberScreen() {
// here can add new members remove members , list mebers each member clickbale card shows name on top and on click opens that member diary, and from there can edit name etc, or amm,

}



@Composable
fun EntryScreen() {
// this is the main logic and and purpose of app,  a voice cammand based filed mapping adn data entrie in tabula form in tables,
    // the table contains 5 columns, 1st serial no, 2nd name, 3rd fat, 4th milk qty, 5th ammount to pay.
    // the ammuont will be caluted by the the fat rate and muliplication of milk qty. the fat rates were seted from home screen button of " set fate rate".
    // the table will grow dynamically as the data entris increse measne rows
    // the data will filled by voice commande, the voice willl mao t the fields and store write values like"name:jeet","fat:6.7","qty:13"
    // there will be a start button on top of the table entrie screen to start voice command, once the started and sayed the data it will automitaly stoep and data will be soterd , again if want to fill entires click on start command and say key value pairs.
    // the app will not close till the data not saved.or removed.
    // now i need to make first the entity and dao and then cretaeing table and then enabling voice command and adding start buuton and seetting auto off,
    // and then just mapoing logic. i will used to map the name with the availabe members in the app and if 70 % mathecs then write that name in table so taht even the user says ine char less or voice not clear we will get the corretc name in table by comparing with existing menbers name.


}
*/
//@Composable
//fun EntryListScreen() {
//    // here display list of entires by date , in two colours blaock for nohgt entries , blue for morning enteris each card shos the date like 5 jun 2025 and when :night or morning. and serail no too.,
//    // a saech bar on top of this screen to seach entreis by date, and a filter button to filter by night moring, or day etc.
//    // on click the tabular form of data entteri will sown up that can be editabel,
//
//}

