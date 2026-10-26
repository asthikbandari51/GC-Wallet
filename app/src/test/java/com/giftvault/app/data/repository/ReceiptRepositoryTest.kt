package com.giftvault.app.data.repository

import com.giftvault.app.core.database.dao.ReceiptDao
import com.giftvault.app.core.database.entity.ReceiptEntity
import com.giftvault.app.core.receipts.ReceiptStorageManager
import com.giftvault.app.domain.model.Receipt
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

class ReceiptRepositoryTest {

    private lateinit var fakeDao: FakeReceiptDao
    private lateinit var repository: ReceiptRepositoryImpl

    @Before
    fun setUp() {
        fakeDao = FakeReceiptDao()
        // Pass fake storage manager mock or null context handle
        val fakeStorageManager = FakeReceiptStorageManager(fakeDao)
        repository = ReceiptRepositoryImpl(fakeDao, fakeStorageManager)
    }

    @Test
    fun `addReceipt inserts receipt into DAO`() = runTest {
        val receipt = Receipt(
            id = "r1",
            cardId = "c1",
            filePath = "/path/to/receipt.jpg",
            createdAt = Instant.now()
        )

        repository.addReceipt(receipt)

        val receipts = repository.getReceiptsForCard("c1").first()
        assertEquals(1, receipts.size)
        assertEquals("r1", receipts[0].id)
        assertEquals("/path/to/receipt.jpg", receipts[0].filePath)
    }

    @Test
    fun `deleteReceipt removes receipt from DAO`() = runTest {
        val receipt = Receipt(
            id = "r1",
            cardId = "c1",
            filePath = "/path/to/receipt.jpg",
            createdAt = Instant.now()
        )

        repository.addReceipt(receipt)
        repository.deleteReceipt("r1", null)

        val receipts = repository.getReceiptsForCard("c1").first()
        assertTrue(receipts.isEmpty())
    }

    @Test
    fun `deleteReceiptsForCard deletes all receipts matching cardId`() = runTest {
        repository.addReceipt(Receipt("r1", "c1", "/p1.jpg", Instant.now()))
        repository.addReceipt(Receipt("r2", "c1", "/p2.jpg", Instant.now()))
        repository.addReceipt(Receipt("r3", "c2", "/p3.jpg", Instant.now()))

        repository.deleteReceiptsForCard("c1")

        val c1Receipts = repository.getReceiptsForCard("c1").first()
        val c2Receipts = repository.getReceiptsForCard("c2").first()

        assertTrue(c1Receipts.isEmpty())
        assertEquals(1, c2Receipts.size)
    }
}

private class FakeReceiptDao : ReceiptDao {
    private val receipts = MutableStateFlow<List<ReceiptEntity>>(emptyList())

    override fun getReceiptsForCard(cardId: String): Flow<List<ReceiptEntity>> {
        return receipts.map { list -> list.filter { it.cardId == cardId } }
    }

    override fun getAllReceipts(): Flow<List<ReceiptEntity>> {
        return receipts
    }

    override suspend fun insertReceipt(receipt: ReceiptEntity) {
        receipts.value = receipts.value.filterNot { it.id == receipt.id } + receipt
    }

    override suspend fun deleteReceipt(receipt: ReceiptEntity) {
        receipts.value = receipts.value.filterNot { it.id == receipt.id }
    }

    override suspend fun deleteReceiptById(receiptId: String) {
        receipts.value = receipts.value.filterNot { it.id == receiptId }
    }

    override suspend fun deleteReceiptsForCard(cardId: String) {
        receipts.value = receipts.value.filterNot { it.cardId == cardId }
    }
}

private class FakeReceiptStorageManager(private val dao: ReceiptDao) : ReceiptStorageManager(
    context = null as Nothing?,
    receiptDao = dao,
    imageProcessor = null as Nothing?
) {
    override suspend fun deleteCardReceiptsDirectory(cardId: String) {
        // No-op for fake test
    }
}
