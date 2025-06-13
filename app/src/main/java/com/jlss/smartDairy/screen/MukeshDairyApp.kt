package com.jlss.smartDairy.screen

import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import com.jlss.smartDairy.navigation.AppNavigation

@RequiresApi(35)
@Composable
fun MukeshDairyApp() {
    // we need routing navaigation fom welcome screen to create profile screen to mainapp screen.
    // i think instead of rendering with true false . let render via appnavigations. and screens. we will set at thir all argless or eith arg.
    // now from here one by one part will be aranged first lets create welcome page with explainantipn how to use the app what app will helps you and its majour features.
    // so here i wnat ui desinged well mease top bar says app name , middle have list of cards with differtn colrts each . acn each card shos its purpus headline, then all about tha toipc like features carda, how to use card, about develpoper jlss, atleast three cards, with one by one slide form
   // at the bottom a footer to include powerd by jlss.
    // and in btw footer and middle card slides , a button to start whihc will lead the user to go in set up profile screen,
    // so for this all we need to use statders things and make it grreat. use what ever we can use so that we can learn too by builind this welcome screen once this made then i am also able to made aps other scrrens.
    AppNavigation()
    // i think we also need bottom or top navigations bar, logic here after the user proced to main app means after welocme afate raccount createin.
}