package com.rhetorica.app.feature.idiom

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rhetorica.app.data.local.DictionaryDao
import com.rhetorica.app.data.local.IdiomEntity
import com.rhetorica.app.data.local.QuoteDao
import com.rhetorica.app.data.repository.IdiomRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class IdiomDetailViewModel @Inject constructor(
    idiomRepository: IdiomRepository,
    private val quoteDao: QuoteDao,
    private val dictionaryDao: DictionaryDao,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val idiomId: Long = savedStateHandle.get<Long>("idiomId") ?: -1L

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<IdiomDetailUiState> = idiomRepository.observeById(idiomId)
        .flatMapLatest { idiom -> flow { emit(assemble(idiom)) } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = IdiomDetailUiState(isLoading = true),
        )

    private suspend fun assemble(idiom: IdiomEntity?): IdiomDetailUiState {
        if (idiom == null) return IdiomDetailUiState(isLoading = false)
        val quote = idiom.quoteId
            ?.let { quoteDao.getQuoteById(it) }
            ?.takeIf { linked -> idiom.oratorId != null && linked.oratorId == idiom.oratorId }
        val oratorName = idiom.oratorId?.let { dictionaryDao.getDictionaryById(it)?.oratorName }
        return IdiomDetailUiState(
            isLoading = false,
            idiom = idiom,
            oratorName = oratorName,
            quoteText = quote?.text,
            quoteSource = quote?.source,
        )
    }
}

data class IdiomDetailUiState(
    val isLoading: Boolean = false,
    val idiom: IdiomEntity? = null,
    val oratorName: String? = null,
    val quoteText: String? = null,
    val quoteSource: String? = null,
)
