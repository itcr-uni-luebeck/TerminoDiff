package terminodiff.terminodiff.ui.panes.conceptmap.mapping

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import terminodiff.terminodiff.engine.conceptmap.ConceptMapState


@Composable
fun ConceptMappingEditorContent(conceptMapState: ConceptMapState) {
    // TODO: 23/02/22 implement concept editor
    val scrollState = rememberScrollState()
    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(scrollState)) {
        Text("""
            Lorem ipsum dolor sit amet, consectetur adipiscing elit. Maecenas gravida justo dui, ut auctor felis vehicula at. Aenean at tristique purus, nec varius lorem. Aliquam erat volutpat. In mollis nulla in neque rutrum ultricies. Etiam mollis semper erat. Ut pellentesque, massa id efficitur rhoncus, velit massa efficitur ipsum, non scelerisque felis ex id risus. Nam sed accumsan lorem. Curabitur id felis at libero condimentum consequat at vel erat. Nullam sed magna et nibh sagittis vulputate a vel neque. Nam quis scelerisque neque, ac venenatis massa. Morbi hendrerit eros vel urna eleifend blandit. Sed eget luctus risus, et tristique lorem. Maecenas eget nunc a ante luctus aliquam. Proin auctor vehicula ultrices.

            Sed at mollis elit. Praesent magna mi, molestie nec gravida sed, feugiat vitae quam. Ut eget sapien eros. Nulla id eros id felis efficitur maximus. Nam lobortis vitae nisl sed accumsan. Proin molestie mauris sed odio tristique, ut molestie lorem cursus. Maecenas fermentum, ligula sed placerat vehicula, metus turpis malesuada risus, et elementum urna tellus et mi. Morbi lacinia sem sed eros consectetur scelerisque.

            Suspendisse scelerisque ante quis mattis fermentum. Vivamus aliquam mollis purus, sit amet blandit odio semper in. Etiam sed sodales turpis, nec commodo dolor. Fusce tempor aliquam convallis. Donec maximus rutrum odio sit amet pellentesque. Praesent sit amet turpis pharetra, elementum justo sit amet, convallis purus. Ut ornare congue risus sit amet gravida. Mauris ultrices ornare mauris ut luctus. Quisque pharetra elit quam, ac pretium erat rutrum et. Vestibulum eleifend laoreet felis a aliquet. Duis facilisis tortor et scelerisque volutpat. Cras eget bibendum libero. Phasellus at gravida ipsum, tempor luctus nisi. Fusce interdum commodo arcu, eget malesuada risus condimentum eu. Proin congue velit ut metus accumsan lacinia.

            Mauris mauris sem, dignissim eget facilisis eget, sodales sit amet elit. Nam vehicula est id metus ultrices, vel maximus diam fringilla. Etiam condimentum accumsan felis ac consectetur. Sed quis tincidunt nulla. Sed scelerisque nisl ante. Aenean eleifend semper risus a aliquam. Suspendisse auctor nisl eget velit consequat, non commodo risus condimentum. Sed commodo tellus eu consequat ultrices. Donec non laoreet nisl. Sed viverra dui ac metus rhoncus interdum. Curabitur a ullamcorper purus. Aenean purus mi, aliquet sit amet quam ut, rutrum dapibus risus. Fusce efficitur elit et justo dictum viverra.

            Fusce placerat neque vitae semper bibendum. Aliquam vitae lectus rhoncus, posuere sapien et, accumsan quam. Curabitur nec mauris in dui sagittis sollicitudin. Nunc eget nunc dapibus, fermentum tellus ut, dapibus nunc. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia curae; Nunc eu ultricies mi, vel rhoncus lacus. Etiam in vehicula arcu. Duis fringilla, augue et sollicitudin rhoncus, mauris mauris lacinia quam, vitae euismod enim odio ac nibh. Nunc eu ex aliquam, lobortis metus in, faucibus ligula. Vestibulum eget tellus vel magna feugiat vehicula sit amet non libero. In sit amet mauris eu sem lacinia cursus. Nunc dictum sapien at placerat tempor. Curabitur porttitor ullamcorper sapien, id luctus est aliquam eget. Nam dictum, ex non tempus faucibus, quam diam maximus odio, vitae imperdiet tellus eros eu purus. Cras eget varius arcu. Sed faucibus vitae ante a mattis.
        """.trimIndent())

    }
}