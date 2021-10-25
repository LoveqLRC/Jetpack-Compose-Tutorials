package com.smarttoolfactory.tutorial1_1basics.chapter3_layout

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.smarttoolfactory.tutorial1_1basics.chapter2_material_widgets.ChatAppbar
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun Tutorial3_6Screen() {

    val systemUiController = rememberSystemUiController()

    DisposableEffect(key1 = true, effect = {

        systemUiController.setStatusBarColor(
            color = Color(0xff00897B)
        )
        onDispose {
            systemUiController.setStatusBarColor(
                color = Color.Transparent
            )
        }
    })

    TutorialContent()
}

@Composable
private fun TutorialContent() {

    val messages = remember { mutableStateListOf<ChatMessage>() }
    val sdf = remember { SimpleDateFormat("hh:mm a", Locale.ROOT) }

    println("🎃 Tutorial3_6Screen messages: $messages, sdf: $sdf")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xffFBE9E7))
    ) {

        ChatAppbar()

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(top = 8.dp, bottom = 8.dp)
        ) {
            items(messages) { message: ChatMessage ->
                MessageRow(
                    text = message.message,
                    messageTime = sdf.format(message.date),
                    messageStatus = MessageStatus.RECEIVED
                )
            }
        }

        ChatInput(onMessageChange = { messageContent ->
            messages.add(
                ChatMessage(
                    (messages.size + 1).toLong(),
                    messageContent,
                    System.currentTimeMillis()
                )
            )
        })
    }
}

@Composable
private fun ChatFlexBoxLayout(
    modifier: Modifier = Modifier,
    text: String,
    messageTime: String,
    messageStatus: MessageStatus
) {

    var lineCount = 1

    val content = @Composable {
        Message(text) { textLayoutResult ->
            lineCount = textLayoutResult.lineCount
        }
        MessageTimeText(messageTime = messageTime, messageStatus = messageStatus)
    }

    Layout(
        modifier = modifier,
        content = content
    ) { measurables: List<Measurable>, constraints: Constraints ->

        if (measurables.size != 2)
            throw IllegalArgumentException("There should be 2 components for this layout")

        val parentWidth = constraints.maxWidth

        val placeables: List<Placeable> = measurables.map { measurable ->
            // Measure each child
            measurable.measure(constraints)
        }

        val totalSize = placeables.fold(IntSize.Zero) { totalSize: IntSize, placeable: Placeable ->

            val intSize = IntSize(
                width = totalSize.width + placeable.width,
                height = totalSize.height + placeable.height
            )

            println(
                "🍏 ChatFlexBoxLayout placeable " +
                        "width: ${placeable.width}, " +
                        "height: ${placeable.height}, " +
                        "intSize: $intSize"
            )

            intSize
        }

        val width: Int
        val height: Int
        var selection = 0

        // Message + time layout is smaller than component so layout them side by side
        if (parentWidth > totalSize.width) {
            width = totalSize.width
            height = placeables.maxOf { it.height }
            selection = 0
        } else {
            width = parentWidth
            height = totalSize.height
            selection = 1
        }

        println("🍒 ChatFlexBoxLayout parentWidth: $parentWidth, maxSize: $totalSize, lineCount: $lineCount, selection: $selection, width: $width, height: $height")

        layout(width = width, height = height) {

            if (selection == 0) {
                placeables.first().placeRelative(0, 0)
                placeables.last()
                    .placeRelative(placeables.first().width, height - placeables.last().height)
            } else {
                placeables.first().placeRelative(0, 0)
                placeables.last()
                    .placeRelative(width - placeables.last().width, placeables.first().height)
            }
        }
    }
}

@Composable
private fun Message(text: String, onTextLayout: (TextLayoutResult) -> Unit) {
    Text(
        modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
        fontSize = 16.sp,
        lineHeight = 18.sp,
        text = text,
        onTextLayout = onTextLayout
    )
}

@Composable
private fun MessageTimeText(
    modifier: Modifier = Modifier,
    messageTime: String,
    messageStatus: MessageStatus
) {

    Row(
        modifier = modifier.padding(end = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            Text(
                modifier = Modifier
                    .padding(top = 1.dp, bottom = 1.dp),
                text = messageTime,
                fontSize = 12.sp
            )
        }

        Icon(
            modifier = Modifier
                .size(16.dp, 12.dp)
                .padding(start = 4.dp),
            imageVector = if (messageStatus == MessageStatus.RECEIVED) {
                Icons.Default.Done
            } else Icons.Default.DoneAll,
            tint = if (messageStatus == MessageStatus.RECEIVED) Color(0xff424242)
            else Color(
                0xff0288D1
            ),
            contentDescription = "state"
        )

    }
}

enum class MessageStatus {
    RECEIVED, READ
}

data class ChatMessage(val id: Long, var message: String, var date: Long)

@Composable
fun MessageRow(
    text: String,
    messageTime: String,
    messageStatus: MessageStatus
) {
    Column(
        horizontalAlignment = Alignment.End,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(top = 1.dp, bottom = 1.dp)
            .background(Color.LightGray)
    ) {
        ChatFlexBoxLayout(

            modifier = Modifier
                .padding(start = 60.dp, end = 8.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xffDCF8C6)),
            text,
            messageTime,
            messageStatus
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun ChatInput(modifier: Modifier = Modifier, onMessageChange: (String) -> Unit) {

    var input by remember { mutableStateOf(TextFieldValue("")) }
    val textEmpty: Boolean by derivedStateOf { input.text.isEmpty() }

    Row(
        modifier = modifier
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.Bottom
    ) {

        ChatTextField(
            modifier = modifier.weight(1f),
            input = input,
            empty = textEmpty,
            onValueChange = {
                input = it
            }
        )

        Spacer(modifier = Modifier.width(6.dp))

        FloatingActionButton(
            modifier = Modifier.size(48.dp),
            backgroundColor = Color(0xff00897B),
            onClick = {
                if (!textEmpty) {
                    onMessageChange(input.text)
                    input = TextFieldValue("")
                }
            }
        ) {
            Icon(
                tint = Color.White,
                imageVector = if (textEmpty) Icons.Filled.Mic else Icons.Filled.Send,
                contentDescription = null
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun ChatTextField(
    modifier: Modifier = Modifier,
    input: TextFieldValue,
    empty: Boolean,
    onValueChange: (TextFieldValue) -> Unit
) {

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colors.surface,
        elevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .padding(2.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {

                CustomIconButton(
                    onClick = { /*TODO*/ },
                    modifier = Modifier.then(Modifier.size(circleButtonSize))
                ) {
                    Icon(imageVector = Icons.Default.Mood, contentDescription = "emoji")
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = circleButtonSize),
                    contentAlignment = Alignment.CenterStart
                ) {

                    BasicTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        textStyle = TextStyle(
                            fontSize = 18.sp
                        ),
                        value = input,
                        onValueChange = onValueChange,
                        cursorBrush = SolidColor(Color(0xff00897B)),
                        decorationBox = { innerTextField ->
                            if (empty) {
                                Text("Message", fontSize = 18.sp)
                            }
                            innerTextField()
                        }
                    )
                }

                CustomIconButton(
                    onClick = { /*TODO*/ },
                    modifier = Modifier.then(Modifier.size(circleButtonSize))
                ) {
                    Icon(
                        modifier = Modifier.rotate(-45f),
                        imageVector = Icons.Default.AttachFile,
                        contentDescription = "attach"
                    )
                }
                AnimatedVisibility(visible = empty) {
                    CustomIconButton(
                        onClick = { /*TODO*/ },
                        modifier = Modifier.then(Modifier.size(circleButtonSize))
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CameraAlt,
                            contentDescription = "camera"
                        )
                    }
                }
            }
        }
    }
}

val circleButtonSize = 44.dp

@Composable
private fun CustomIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit
) {

    Box(
        modifier = modifier
            .clickable(
                onClick = onClick,
                enabled = enabled,
                role = Role.Button,
                interactionSource = interactionSource,
                indication = rememberRipple(bounded = false, radius = circleButtonSize / 2)
            ),
        contentAlignment = Alignment.Center
    ) {
        val contentAlpha = if (enabled) LocalContentAlpha.current else ContentAlpha.disabled
        CompositionLocalProvider(LocalContentAlpha provides contentAlpha, content = content)
    }
}

@Preview
@Preview("dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(device = Devices.PIXEL_C)
@Composable
private fun CustomIconButtonPreview() {
    CustomIconButton(onClick = {}) {
        Icon(
            imageVector = Icons.Filled.CameraAlt,
            contentDescription = "camera"
        )
    }
}

@Preview
@Preview("dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(device = Devices.PIXEL_C)
@Composable
private fun ChatInputPreview() {
    ChatInput() {}
}
