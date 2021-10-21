import androidx.compose.desktop.DesktopMaterialTheme
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalDesktopApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.mouseClickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.imageFromResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import core.engine.Position
import java.io.File
import java.io.InputStream

@Composable
@Preview
fun Cell(position: Position, value: Int, isSelectable: Boolean, onSelected: (Position) -> Unit) {


    val imageModifier = Modifier
        .height(50.dp)
        .fillMaxWidth()
//        .clip(RoundedCornerShape(12.dp))

    Box {

        if (value == 0)
            Image(
                painter = painterResource("empty.png"),
                contentDescription = "image",
                imageModifier,
                contentScale = ContentScale.Fit
            )

        if (isSelectable)
            Image(
                painter = painterResource("selectable.png"),
                contentDescription = "image",
                imageModifier.clickable {
                    onSelected(position)
                },
                contentScale = ContentScale.Fit
            )

        if (value == 1 || value == 2)
            Image(
                painter = painterResource(if (value == 1) "pawn_black.webp" else "white-pawn.png"),
                contentDescription = "image",
                imageModifier,
                contentScale = ContentScale.Fit
            )

        if (value == 3 || value == 4)
            Image(
                painter = painterResource( "city hall.png"),
                contentDescription = "image",
                imageModifier,
                contentScale = ContentScale.Fit
            )
    }


}


