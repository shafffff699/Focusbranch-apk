package androidx.compose.material.icons.filled

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

private fun simpleMaterialIcon(name: String, block: androidx.compose.ui.graphics.vector.PathBuilder.() -> Unit): ImageVector =
    materialIcon(name = name) {
        materialPath(pathBuilder = block)
    }

val Icons.Filled.Shield: ImageVector by lazy {
    simpleMaterialIcon("Filled.Shield") {
        moveTo(12f, 1f)
        lineTo(3f, 5f)
        verticalLineTo(11f)
        curveTo(3f, 16.55f, 6.84f, 21.74f, 12f, 23f)
        curveTo(17.16f, 21.74f, 21f, 16.55f, 21f, 11f)
        verticalLineTo(5f)
        close()
    }
}

val Icons.Filled.Timer: ImageVector by lazy {
    simpleMaterialIcon("Filled.Timer") {
        moveTo(15f, 1f)
        horizontalLineTo(9f)
        verticalLineTo(3f)
        horizontalLineTo(15f)
        close()
        moveTo(11f, 14f)
        horizontalLineTo(13f)
        verticalLineTo(8f)
        horizontalLineTo(11f)
        close()
        moveTo(19.03f, 7.39f)
        lineTo(20.45f, 5.97f)
        lineTo(19.04f, 4.56f)
        lineTo(17.62f, 5.98f)
        curveTo(16.07f, 4.74f, 14.12f, 4f, 12f, 4f)
        curveTo(7.03f, 4f, 3f, 8.03f, 3f, 13f)
        curveTo(3f, 17.97f, 7.02f, 22f, 12f, 22f)
        curveTo(16.98f, 22f, 21f, 17.97f, 21f, 13f)
        curveTo(21f, 10.88f, 20.26f, 8.93f, 19.03f, 7.39f)
        close()
    }
}

val Icons.Filled.PlayCircleFilled: ImageVector by lazy {
    simpleMaterialIcon("Filled.PlayCircleFilled") {
        moveTo(12f, 2f)
        curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
        curveTo(2f, 17.52f, 6.48f, 22f, 12f, 22f)
        curveTo(17.52f, 22f, 22f, 17.52f, 22f, 12f)
        curveTo(22f, 6.48f, 17.52f, 2f, 12f, 2f)
        close()
        moveTo(10f, 16.5f)
        verticalLineTo(7.5f)
        lineTo(16f, 12f)
        close()
    }
}

val Icons.Filled.Groups: ImageVector by lazy {
    simpleMaterialIcon("Filled.Groups") {
        moveTo(12f, 12.75f)
        curveTo(13.63f, 12.75f, 15.07f, 13.14f, 16.24f, 13.65f)
        curveTo(17.32f, 14.13f, 18f, 15.21f, 18f, 16.38f)
        verticalLineTo(18f)
        horizontalLineTo(6f)
        verticalLineTo(16.38f)
        curveTo(6f, 15.21f, 6.68f, 14.13f, 7.76f, 13.65f)
        curveTo(8.93f, 13.14f, 10.37f, 12.75f, 12f, 12.75f)
        close()
        moveTo(12f, 11f)
        curveTo(10.34f, 11f, 9f, 9.66f, 9f, 8f)
        curveTo(9f, 6.34f, 10.34f, 5f, 12f, 5f)
        curveTo(13.66f, 5f, 15f, 6.34f, 15f, 8f)
        curveTo(15f, 9.66f, 13.66f, 11f, 12f, 11f)
        close()
    }
}

val Icons.Filled.Analytics: ImageVector by lazy {
    simpleMaterialIcon("Filled.Analytics") {
        moveTo(19f, 3f)
        horizontalLineTo(5f)
        curveTo(3.9f, 3f, 3f, 3.9f, 3f, 5f)
        verticalLineTo(19f)
        curveTo(3f, 20.1f, 3.9f, 21f, 5f, 21f)
        horizontalLineTo(19f)
        curveTo(20.1f, 21f, 21f, 20.1f, 21f, 19f)
        verticalLineTo(5f)
        curveTo(21f, 3.9f, 20.1f, 3f, 19f, 3f)
        close()
        moveTo(9f, 17f)
        horizontalLineTo(7f)
        verticalLineTo(12f)
        horizontalLineTo(9f)
        close()
        moveTo(13f, 17f)
        horizontalLineTo(11f)
        verticalLineTo(7f)
        horizontalLineTo(13f)
        close()
        moveTo(17f, 17f)
        horizontalLineTo(15f)
        verticalLineTo(10f)
        horizontalLineTo(17f)
        close()
    }
}

val Icons.Filled.BarChart: ImageVector by lazy {
    Icons.Filled.Analytics
}

val Icons.Filled.AutoAwesome: ImageVector by lazy {
    simpleMaterialIcon("Filled.AutoAwesome") {
        moveTo(19f, 9f)
        lineTo(20.25f, 6.25f)
        lineTo(23f, 5f)
        lineTo(20.25f, 3.75f)
        lineTo(19f, 1f)
        lineTo(17.75f, 3.75f)
        lineTo(15f, 5f)
        lineTo(17.75f, 6.25f)
        close()
        moveTo(11.5f, 9.5f)
        lineTo(9f, 4f)
        lineTo(6.5f, 9.5f)
        lineTo(1f, 12f)
        lineTo(6.5f, 14.5f)
        lineTo(9f, 20f)
        lineTo(11.5f, 14.5f)
        lineTo(17f, 12f)
        close()
    }
}

val Icons.Filled.Block: ImageVector by lazy {
    simpleMaterialIcon("Filled.Block") {
        moveTo(12f, 2f)
        curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
        curveTo(2f, 17.52f, 6.48f, 22f, 12f, 22f)
        curveTo(17.52f, 22f, 22f, 17.52f, 22f, 12f)
        curveTo(22f, 6.48f, 17.52f, 2f, 12f, 2f)
        close()
        moveTo(4f, 12f)
        curveTo(4f, 7.58f, 7.58f, 4f, 12f, 4f)
        curveTo(13.85f, 4f, 15.55f, 4.63f, 16.9f, 5.69f)
        lineTo(5.69f, 16.9f)
        curveTo(4.63f, 15.55f, 4f, 13.85f, 4f, 12f)
        close()
        moveTo(12f, 20f)
        curveTo(10.15f, 20f, 8.45f, 19.37f, 7.1f, 18.31f)
        lineTo(18.31f, 7.1f)
        curveTo(19.37f, 8.45f, 20f, 10.15f, 20f, 12f)
        curveTo(20f, 16.42f, 16.42f, 20f, 12f, 20f)
        close()
    }
}

val Icons.Filled.Bolt: ImageVector by lazy {
    simpleMaterialIcon("Filled.Bolt") {
        moveTo(11f, 21f)
        horizontalLineTo(10f)
        lineTo(11f, 14f)
        horizontalLineTo(7.5f)
        lineTo(13f, 3f)
        horizontalLineTo(14f)
        lineTo(13f, 10f)
        horizontalLineTo(16.5f)
        close()
    }
}

val Icons.Filled.Code: ImageVector by lazy {
    simpleMaterialIcon("Filled.Code") {
        moveTo(9.4f, 16.6f)
        lineTo(4.8f, 12f)
        lineTo(9.4f, 7.4f)
        lineTo(8f, 6f)
        lineTo(2f, 12f)
        lineTo(8f, 18f)
        close()
        moveTo(14.6f, 16.6f)
        lineTo(19.2f, 12f)
        lineTo(14.6f, 7.4f)
        lineTo(16f, 6f)
        lineTo(22f, 12f)
        lineTo(16f, 18f)
        close()
    }
}

val Icons.Filled.ContentCopy: ImageVector by lazy {
    simpleMaterialIcon("Filled.ContentCopy") {
        moveTo(16f, 1f)
        horizontalLineTo(4f)
        curveTo(2.9f, 1f, 2f, 1.9f, 2f, 3f)
        verticalLineTo(17f)
        horizontalLineTo(4f)
        verticalLineTo(3f)
        horizontalLineTo(16f)
        close()
        moveTo(19f, 5f)
        horizontalLineTo(8f)
        curveTo(6.9f, 5f, 6f, 5.9f, 6f, 7f)
        verticalLineTo(21f)
        curveTo(6f, 22.1f, 6.9f, 23f, 8f, 23f)
        horizontalLineTo(19f)
        curveTo(20.1f, 23f, 21f, 22.1f, 21f, 21f)
        verticalLineTo(7f)
        curveTo(21f, 5.9f, 20.1f, 5f, 19f, 5f)
        close()
    }
}

val Icons.Filled.DeleteSweep: ImageVector by lazy {
    simpleMaterialIcon("Filled.DeleteSweep") {
        moveTo(15f, 16f)
        horizontalLineTo(19f)
        verticalLineTo(18f)
        horizontalLineTo(15f)
        close()
        moveTo(15f, 8f)
        horizontalLineTo(22f)
        verticalLineTo(10f)
        horizontalLineTo(15f)
        close()
        moveTo(15f, 12f)
        horizontalLineTo(21f)
        verticalLineTo(14f)
        horizontalLineTo(15f)
        close()
        moveTo(3f, 18f)
        curveTo(3f, 19.1f, 3.9f, 20f, 5f, 20f)
        horizontalLineTo(11f)
        curveTo(12.1f, 20f, 13f, 19.1f, 13f, 18f)
        verticalLineTo(8f)
        horizontalLineTo(3f)
        close()
        moveTo(14f, 5f)
        horizontalLineTo(11f)
        lineTo(10f, 4f)
        horizontalLineTo(6f)
        lineTo(5f, 5f)
        horizontalLineTo(2f)
        verticalLineTo(7f)
        horizontalLineTo(14f)
        close()
    }
}

val Icons.Filled.EmojiEvents: ImageVector by lazy {
    simpleMaterialIcon("Filled.EmojiEvents") {
        moveTo(19f, 5f)
        horizontalLineTo(17f)
        verticalLineTo(3f)
        horizontalLineTo(7f)
        verticalLineTo(5f)
        horizontalLineTo(5f)
        curveTo(3.9f, 5f, 3f, 5.9f, 3f, 7f)
        verticalLineTo(8f)
        curveTo(3f, 10.55f, 4.92f, 12.63f, 7.39f, 12.94f)
        curveTo(8.02f, 14.44f, 9.37f, 15.57f, 11f, 15.9f)
        verticalLineTo(19f)
        horizontalLineTo(7f)
        verticalLineTo(21f)
        horizontalLineTo(17f)
        verticalLineTo(19f)
        horizontalLineTo(13f)
        verticalLineTo(15.9f)
        curveTo(14.63f, 15.57f, 15.98f, 14.44f, 16.61f, 12.94f)
        curveTo(19.08f, 12.63f, 21f, 10.55f, 21f, 8f)
        verticalLineTo(7f)
        curveTo(21f, 5.9f, 20.1f, 5f, 19f, 5f)
        close()
    }
}

val Icons.Filled.ExpandLess: ImageVector by lazy {
    simpleMaterialIcon("Filled.ExpandLess") {
        moveTo(12f, 8f)
        lineTo(6f, 14f)
        lineTo(7.41f, 15.41f)
        lineTo(12f, 10.83f)
        lineTo(16.59f, 15.41f)
        lineTo(18f, 14f)
        close()
    }
}

val Icons.Filled.ExpandMore: ImageVector by lazy {
    simpleMaterialIcon("Filled.ExpandMore") {
        moveTo(16.59f, 8.59f)
        lineTo(12f, 13.17f)
        lineTo(7.41f, 8.59f)
        lineTo(6f, 10f)
        lineTo(12f, 16f)
        lineTo(18f, 10f)
        close()
    }
}

val Icons.Filled.IosShare: ImageVector by lazy {
    simpleMaterialIcon("Filled.IosShare") {
        moveTo(16f, 5f)
        lineTo(14.58f, 6.42f)
        lineTo(12.99f, 4.83f)
        verticalLineTo(16f)
        horizontalLineTo(11.01f)
        verticalLineTo(4.83f)
        lineTo(9.42f, 6.42f)
        lineTo(8f, 5f)
        lineTo(12f, 1f)
        close()
        moveTo(20f, 10f)
        verticalLineTo(21f)
        curveTo(20f, 22.1f, 19.1f, 23f, 18f, 23f)
        horizontalLineTo(6f)
        curveTo(4.89f, 23f, 4f, 22.1f, 4f, 21f)
        verticalLineTo(10f)
        curveTo(4f, 8.89f, 4.89f, 8f, 6f, 8f)
        horizontalLineTo(9f)
        verticalLineTo(10f)
        horizontalLineTo(6f)
        verticalLineTo(21f)
        horizontalLineTo(18f)
        verticalLineTo(10f)
        horizontalLineTo(15f)
        verticalLineTo(8f)
        horizontalLineTo(18f)
        curveTo(19.1f, 8f, 20f, 8.89f, 20f, 10f)
        close()
    }
}

val Icons.Filled.LocalFireDepartment: ImageVector by lazy {
    simpleMaterialIcon("Filled.LocalFireDepartment") {
        moveTo(13.5f, 0.67f)
        curveTo(14.24f, 3.32f, 16.25f, 5.47f, 16.25f, 8.5f)
        curveTo(16.25f, 10.56f, 14.9f, 12.23f, 13f, 12.23f)
        curveTo(10.93f, 12.23f, 9.37f, 10.56f, 9.37f, 8.5f)
        lineTo(9.4f, 8.14f)
        curveTo(7.21f, 10.5f, 6f, 13.62f, 6f, 16.5f)
        curveTo(6f, 19.81f, 8.69f, 22.5f, 12f, 22.5f)
        curveTo(15.31f, 22.5f, 18f, 19.81f, 18f, 16.5f)
        curveTo(18f, 12.32f, 15.88f, 6.29f, 13.5f, 0.67f)
        close()
    }
}

val Icons.Filled.PhotoCamera: ImageVector by lazy {
    simpleMaterialIcon("Filled.PhotoCamera") {
        moveTo(9f, 2f)
        lineTo(7.17f, 4f)
        horizontalLineTo(4f)
        curveTo(2.9f, 4f, 2f, 4.9f, 2f, 6f)
        verticalLineTo(18f)
        curveTo(2f, 19.1f, 2.9f, 20f, 4f, 20f)
        horizontalLineTo(20f)
        curveTo(21.1f, 20f, 22f, 19.1f, 22f, 18f)
        verticalLineTo(6f)
        curveTo(22f, 4.9f, 21.1f, 4f, 20f, 4f)
        horizontalLineTo(16.83f)
        lineTo(15f, 2f)
        close()
        moveTo(12f, 17f)
        curveTo(9.24f, 17f, 7f, 14.76f, 7f, 12f)
        curveTo(7f, 9.24f, 9.24f, 7f, 12f, 7f)
        curveTo(14.76f, 7f, 17f, 9.24f, 17f, 12f)
        curveTo(17f, 14.76f, 14.76f, 17f, 12f, 17f)
        close()
    }
}

val Icons.Filled.Public: ImageVector by lazy {
    simpleMaterialIcon("Filled.Public") {
        moveTo(12f, 2f)
        curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
        curveTo(2f, 17.52f, 6.48f, 22f, 12f, 22f)
        curveTo(17.52f, 22f, 22f, 17.52f, 22f, 12f)
        curveTo(22f, 6.48f, 17.52f, 2f, 12f, 2f)
        close()
        moveTo(11f, 19.93f)
        curveTo(7.05f, 19.44f, 4f, 16.08f, 4f, 12f)
        lineTo(9f, 12f)
        verticalLineTo(15f)
        curveTo(9f, 16.1f, 9.9f, 17f, 11f, 17f)
        close()
    }
}

val Icons.Filled.Quiz: ImageVector by lazy {
    simpleMaterialIcon("Filled.Quiz") {
        moveTo(4f, 6f)
        horizontalLineTo(2f)
        verticalLineTo(20f)
        curveTo(2f, 21.1f, 2.9f, 22f, 4f, 22f)
        horizontalLineTo(18f)
        verticalLineTo(20f)
        horizontalLineTo(4f)
        close()
        moveTo(20f, 2f)
        horizontalLineTo(8f)
        curveTo(6.9f, 2f, 6f, 2.9f, 6f, 4f)
        verticalLineTo(16f)
        curveTo(6f, 17.1f, 6.9f, 18f, 8f, 18f)
        horizontalLineTo(20f)
        curveTo(21.1f, 18f, 22f, 17.1f, 22f, 16f)
        verticalLineTo(4f)
        curveTo(22f, 2.9f, 21.1f, 2f, 20f, 2f)
        close()
    }
}

val Icons.Filled.Security: ImageVector by lazy {
    Icons.Filled.Shield
}

val Icons.Filled.Sms: ImageVector by lazy {
    simpleMaterialIcon("Filled.Sms") {
        moveTo(20f, 2f)
        horizontalLineTo(4f)
        curveTo(2.9f, 2f, 2f, 2.9f, 2f, 4f)
        verticalLineTo(22f)
        lineTo(6f, 18f)
        horizontalLineTo(20f)
        curveTo(21.1f, 18f, 22f, 17.1f, 22f, 16f)
        verticalLineTo(4f)
        curveTo(22f, 2.9f, 21.1f, 2f, 20f, 2f)
        close()
    }
}

val Icons.Filled.Stop: ImageVector by lazy {
    simpleMaterialIcon("Filled.Stop") {
        moveTo(6f, 6f)
        horizontalLineTo(18f)
        verticalLineTo(18f)
        horizontalLineTo(6f)
        close()
    }
}

val Icons.Filled.Storage: ImageVector by lazy {
    simpleMaterialIcon("Filled.Storage") {
        moveTo(2f, 20f)
        horizontalLineTo(22f)
        verticalLineTo(16f)
        horizontalLineTo(2f)
        close()
        moveTo(2f, 4f)
        verticalLineTo(8f)
        horizontalLineTo(22f)
        verticalLineTo(4f)
        close()
        moveTo(2f, 14f)
        horizontalLineTo(22f)
        verticalLineTo(10f)
        horizontalLineTo(2f)
        close()
    }
}

val Icons.Filled.Tune: ImageVector by lazy {
    simpleMaterialIcon("Filled.Tune") {
        moveTo(3f, 17f)
        verticalLineTo(19f)
        horizontalLineTo(9f)
        verticalLineTo(17f)
        close()
        moveTo(3f, 5f)
        verticalLineTo(7f)
        horizontalLineTo(13f)
        verticalLineTo(5f)
        close()
        moveTo(13f, 21f)
        verticalLineTo(19f)
        horizontalLineTo(21f)
        verticalLineTo(17f)
        horizontalLineTo(13f)
        verticalLineTo(15f)
        horizontalLineTo(11f)
        verticalLineTo(21f)
        close()
        moveTo(7f, 9f)
        verticalLineTo(11f)
        horizontalLineTo(3f)
        verticalLineTo(13f)
        horizontalLineTo(7f)
        verticalLineTo(15f)
        horizontalLineTo(9f)
        verticalLineTo(9f)
        close()
        moveTo(21f, 13f)
        verticalLineTo(11f)
        horizontalLineTo(11f)
        verticalLineTo(13f)
        close()
        moveTo(15f, 9f)
        horizontalLineTo(17f)
        verticalLineTo(7f)
        horizontalLineTo(21f)
        verticalLineTo(5f)
        horizontalLineTo(17f)
        verticalLineTo(3f)
        horizontalLineTo(15f)
        close()
    }
}

val Icons.Filled.Verified: ImageVector by lazy {
    simpleMaterialIcon("Filled.Verified") {
        moveTo(23f, 12f)
        lineTo(20.56f, 9.21f)
        lineTo(20.9f, 5.52f)
        lineTo(17.29f, 4.7f)
        lineTo(15.4f, 1.5f)
        lineTo(12f, 2.96f)
        lineTo(8.6f, 1.5f)
        lineTo(6.71f, 4.69f)
        lineTo(3.1f, 5.5f)
        lineTo(3.44f, 9.2f)
        lineTo(1f, 12f)
        lineTo(3.44f, 14.79f)
        lineTo(3.1f, 18.49f)
        lineTo(6.71f, 19.31f)
        lineTo(8.6f, 22.5f)
        lineTo(12f, 21.03f)
        lineTo(15.4f, 22.49f)
        lineTo(17.29f, 19.3f)
        lineTo(20.9f, 18.48f)
        lineTo(20.56f, 14.79f)
        close()
        moveTo(10.09f, 16.72f)
        lineTo(6.29f, 12.91f)
        lineTo(7.77f, 11.43f)
        lineTo(10.09f, 13.76f)
        lineTo(15.94f, 7.89f)
        lineTo(17.42f, 9.37f)
        close()
    }
}

val Icons.Filled.VisibilityOff: ImageVector by lazy {
    simpleMaterialIcon("Filled.VisibilityOff") {
        moveTo(12f, 7f)
        curveTo(14.76f, 7f, 17f, 9.24f, 17f, 12f)
        curveTo(17f, 12.65f, 16.87f, 13.26f, 16.64f, 13.83f)
        lineTo(19.56f, 16.75f)
        curveTo(21.07f, 15.49f, 22.26f, 13.86f, 22.99f, 12f)
        curveTo(21.26f, 7.61f, 16.99f, 4.5f, 11.99f, 4.5f)
        curveTo(10.59f, 4.5f, 9.25f, 4.75f, 8.01f, 5.2f)
        lineTo(10.17f, 7.36f)
        curveTo(10.74f, 7.13f, 11.35f, 7f, 12f, 7f)
        close()
        moveTo(2f, 4.27f)
        lineTo(4.28f, 6.55f)
        lineTo(4.74f, 7.01f)
        curveTo(3.08f, 8.3f, 1.78f, 10.02f, 1f, 12f)
        curveTo(2.73f, 16.39f, 7f, 19.5f, 12f, 19.5f)
        curveTo(13.55f, 19.5f, 15.03f, 19.2f, 16.38f, 18.66f)
        lineTo(16.8f, 19.08f)
        lineTo(19.73f, 22f)
        lineTo(21f, 20.73f)
        lineTo(3.27f, 3f)
        close()
    }
}

val Icons.Filled.WarningAmber: ImageVector by lazy {
    simpleMaterialIcon("Filled.WarningAmber") {
        moveTo(1f, 21f)
        horizontalLineTo(23f)
        lineTo(12f, 2f)
        close()
        moveTo(13f, 18f)
        horizontalLineTo(11f)
        verticalLineTo(16f)
        horizontalLineTo(13f)
        close()
        moveTo(13f, 14f)
        horizontalLineTo(11f)
        verticalLineTo(10f)
        horizontalLineTo(13f)
        close()
    }
}
