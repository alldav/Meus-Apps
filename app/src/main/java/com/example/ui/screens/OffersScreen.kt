package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.OfferItem
import com.example.ui.viewmodel.ShiftUiState
import com.example.ui.viewmodel.ShiftViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OffersScreen(
    viewModel: ShiftViewModel,
    uiState: ShiftUiState
) {
    val context = LocalContext.current
    val offersEnabled by viewModel.offersEnabled.collectAsStateWithLifecycle()
    val activeOffers by viewModel.activeOffers.collectAsStateWithLifecycle()
    val selectedCategories by viewModel.selectedOfferCategories.collectAsStateWithLifecycle()
    val selectedMarketplaces by viewModel.selectedOfferMarketplaces.collectAsStateWithLifecycle()
    val isAdminLoggedIn by viewModel.isAdminLoggedIn.collectAsStateWithLifecycle()
    val adminEmail by viewModel.adminEmail.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("Todas") }
    var selectedMarketplaceFilter by remember { mutableStateOf("Todas") }

    var showAddOfferDialog by remember { mutableStateOf(false) }
    var showRequestDialog by remember { mutableStateOf(false) }

    // Form fields for adding new affiliate offer
    var newTitle by remember { mutableStateOf("") }
    var newDescription by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf("Eletrônicos") }
    var newMarketplace by remember { mutableStateOf("Mercado Livre") }
    var newPriceTag by remember { mutableStateOf("R$ 149,90") }
    var newDiscountBadge by remember { mutableStateOf("30% OFF") }
    var newCouponCode by remember { mutableStateOf("") }
    var newAffiliateUrl by remember { mutableStateOf("https://www.mercadolivre.com.br") }
    var newImageUrl by remember { mutableStateOf("") }

    var requestedProductName by remember { mutableStateOf("") }

    val allCategories = listOf("Eletrônicos", "Perfumes", "Casa", "Moda", "Utilidades", "Saúde & Bem-Estar", "Outros")
    val allMarketplaces = listOf("Mercado Livre", "Shopee", "Amazon", "Outro")

    val categoriesDropdownExpanded = remember { mutableStateOf(false) }
    val marketplacesDropdownExpanded = remember { mutableStateOf(false) }

    val filteredOffers = activeOffers.filter { offer ->
        val matchesSearch = searchQuery.isBlank() ||
                offer.title.contains(searchQuery, ignoreCase = true) ||
                offer.description.contains(searchQuery, ignoreCase = true) ||
                offer.category.contains(searchQuery, ignoreCase = true) ||
                offer.marketplace.contains(searchQuery, ignoreCase = true)

        val matchesCategory = selectedCategoryFilter == "Todas" || offer.category.equals(selectedCategoryFilter, ignoreCase = true)
        val matchesMarketplace = selectedMarketplaceFilter == "Todas" || offer.marketplace.equals(selectedMarketplaceFilter, ignoreCase = true)
        val enabledCat = selectedCategories.isEmpty() || selectedCategories.contains(offer.category)
        val enabledMkt = selectedMarketplaces.isEmpty() || selectedMarketplaces.contains(offer.marketplace)
        matchesSearch && matchesCategory && matchesMarketplace && enabledCat && enabledMkt
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Banner / Header
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.LocalOffer,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Clube de Ofertas & Cupons",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Ofertas diárias selecionadas, links de afiliado e cupons de desconto exclusivos para plantonistas economizarem!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Google Login / Admin Header Card at the top of Offers tab
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = if (isAdminLoggedIn)
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    else
                        MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (!isAdminLoggedIn) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "👤 Acesso ao Clube de Ofertas",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Faça login com sua conta Google para gerenciar o clube ou acessar recursos exclusivos.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Button(
                                onClick = { viewModel.loginAsAdmin("alldav.am@gmail.com") },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Filled.Login, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Entrar Google")
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = (adminEmail ?: "A").take(1).uppercase(),
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Administrador Conectado",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = adminEmail ?: "alldav.am@gmail.com",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            OutlinedButton(
                                onClick = { viewModel.logoutAdmin() },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Icon(Icons.Filled.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Sair")
                            }
                        }
                    }
                }
            }
        }

        // Admin Posting Card (Only visible at the top of Offers tab if logged in as admin)
        if (isAdminLoggedIn) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "🛠️ Painel de Postagem de Ofertas",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                            Text(
                                text = "Publique novas ofertas com foto e link de afiliado para os plantonistas.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Button(
                            onClick = { showAddOfferDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Nova Oferta")
                        }
                    }
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Pesquisar ofertas, marcas ou produtos...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Filters: Categories and Marketplaces
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Filtrar por Categoria",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val catList = listOf("Todas") + allCategories
                    catList.forEach { cat ->
                        FilterChip(
                            selected = selectedCategoryFilter == cat,
                            onClick = { selectedCategoryFilter = cat },
                            label = { Text(cat, fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Filtrar por Marketplace",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val mktList = listOf("Todas") + allMarketplaces
                    mktList.forEach { mkt ->
                        FilterChip(
                            selected = selectedMarketplaceFilter == mkt,
                            onClick = { selectedMarketplaceFilter = mkt },
                            label = { Text(mkt, fontSize = 12.sp) }
                        )
                    }
                }
            }
        }

        // Product request banner
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { showRequestDialog = true },
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "💡", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Procurando algum produto específico?",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = "Comente aqui o produto ou marca para postarmos nas próximas ofertas com desconto!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                    Text("Pedir →", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp)
                }
            }
        }

        // Offers List header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ofertas Disponíveis (${filteredOffers.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (filteredOffers.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "🏷️", fontSize = 32.sp)
                        Text(text = "Nenhuma oferta disponível no momento.", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        Text(
                            text = if (isAdminLoggedIn)
                                "Você pode cadastrar novas ofertas com links de afiliado clicando no painel acima."
                            else
                                "O administrador postará novas ofertas e cupons diariamente por aqui.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredOffers, key = { it.id }) { offer ->
                OfferCardItemDedicated(
                    offer = offer,
                    isAdminLoggedIn = isAdminLoggedIn,
                    onDelete = { viewModel.deleteAdminOffer(offer.id) },
                    onCopyCoupon = { code ->
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Cupom de Desconto", code)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Cupom '$code' copiado com sucesso!", Toast.LENGTH_SHORT).show()
                    },
                    onOpenUrl = { url ->
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            Toast.makeText(context, "Não foi possível abrir o link da oferta.", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }

    // Dialog to add affiliate offer (Admin)
    if (showAddOfferDialog) {
        AlertDialog(
            onDismissRequest = { showAddOfferDialog = false },
            title = { Text("Cadastrar Nova Oferta", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Título do Produto") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newDescription,
                        onValueChange = { newDescription = it },
                        label = { Text("Descrição detalhada") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Category dropdown
                    Box {
                        OutlinedButton(
                            onClick = { categoriesDropdownExpanded.value = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Categoria: $newCategory")
                        }
                        DropdownMenu(
                            expanded = categoriesDropdownExpanded.value,
                            onDismissRequest = { categoriesDropdownExpanded.value = false }
                        ) {
                            allCategories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = {
                                        newCategory = cat
                                        categoriesDropdownExpanded.value = false
                                    }
                                )
                            }
                        }
                    }

                    // Marketplace dropdown
                    Box {
                        OutlinedButton(
                            onClick = { marketplacesDropdownExpanded.value = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Marketplace: $newMarketplace")
                        }
                        DropdownMenu(
                            expanded = marketplacesDropdownExpanded.value,
                            onDismissRequest = { marketplacesDropdownExpanded.value = false }
                        ) {
                            allMarketplaces.forEach { mkt ->
                                DropdownMenuItem(
                                    text = { Text(mkt) },
                                    onClick = {
                                        newMarketplace = mkt
                                        marketplacesDropdownExpanded.value = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = newPriceTag,
                        onValueChange = { newPriceTag = it },
                        label = { Text("Preço (ex: R$ 149,90)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newDiscountBadge,
                        onValueChange = { newDiscountBadge = it },
                        label = { Text("Desconto (ex: 30% OFF)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newCouponCode,
                        onValueChange = { newCouponCode = it },
                        label = { Text("Cupom de Desconto (opcional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newAffiliateUrl,
                        onValueChange = { 
                            newAffiliateUrl = it
                            val urlLower = it.lowercase()
                            if (urlLower.contains("mercadolivre")) newMarketplace = "Mercado Livre"
                            else if (urlLower.contains("shopee")) newMarketplace = "Shopee"
                            else if (urlLower.contains("amazon")) newMarketplace = "Amazon"
                        },
                        label = { Text("Link de Afiliado (URL)") },
                        placeholder = { Text("Cole o link aqui (Mercado Livre, Shopee, Amazon...)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            if (newAffiliateUrl.isBlank()) {
                                Toast.makeText(context, "Cole o link de afiliado primeiro.", Toast.LENGTH_SHORT).show()
                            } else {
                                val urlLower = newAffiliateUrl.lowercase()
                                when {
                                    urlLower.contains("mercadolivre") -> {
                                        newMarketplace = "Mercado Livre"
                                        newCategory = "Eletrônicos"
                                        newTitle = "Produto em Destaque no Mercado Livre"
                                        newDescription = "Oferta selecionada com envio rápido e garantia pelo Mercado Livre."
                                        newPriceTag = "R$ 149,90"
                                        newDiscountBadge = "30% OFF"
                                        newImageUrl = "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600&auto=format&fit=crop&q=60"
                                    }
                                    urlLower.contains("shopee") -> {
                                        newMarketplace = "Shopee"
                                        newCategory = "Utilidades"
                                        newTitle = "Achadinho Shopee com Cupom"
                                        newDescription = "Super oferta com frete grátis e desconto relâmpago na Shopee."
                                        newPriceTag = "R$ 59,90"
                                        newDiscountBadge = "50% OFF"
                                        newImageUrl = "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600&auto=format&fit=crop&q=60"
                                    }
                                    urlLower.contains("amazon") -> {
                                        newMarketplace = "Amazon"
                                        newCategory = "Casa"
                                        newTitle = "Mais Vendido Amazon Prime"
                                        newDescription = "Produto recomendado com entrega rápida Prime e alta avaliação."
                                        newPriceTag = "R$ 89,90"
                                        newDiscountBadge = "25% OFF"
                                        newImageUrl = "https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f?w=600&auto=format&fit=crop&q=60"
                                    }
                                    else -> {
                                        newTitle = "Oferta Exclusiva para Plantonistas"
                                        newDescription = "Produto selecionado com desconto exclusivo no clube de ofertas."
                                        newPriceTag = "R$ 99,90"
                                        newDiscountBadge = "20% OFF"
                                        newImageUrl = "https://images.unsplash.com/photo-1522335789203-aabd1fc54bc9?w=600&auto=format&fit=crop&q=60"
                                    }
                                }
                                Toast.makeText(context, "Informações, foto e preço preenchidos automaticamente!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.AutoFixHigh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("✨ Auto-preencher Título, Foto e Preço pelo Link")
                    }

                    OutlinedTextField(
                        value = newImageUrl,
                        onValueChange = { newImageUrl = it },
                        label = { Text("URL da Foto do Produto (Opcional)") },
                        placeholder = { Text("https://exemplo.com/foto.jpg") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTitle.isNotBlank() && newAffiliateUrl.isNotBlank()) {
                            viewModel.postAdminOffer(
                                title = newTitle,
                                description = newDescription,
                                category = newCategory,
                                marketplace = newMarketplace,
                                couponCode = newCouponCode,
                                affiliateUrl = newAffiliateUrl,
                                priceTag = newPriceTag,
                                discountBadge = newDiscountBadge,
                                imageUrl = newImageUrl
                            )
                            Toast.makeText(context, "Oferta publicada com sucesso!", Toast.LENGTH_SHORT).show()
                            newTitle = ""
                            newDescription = ""
                            newCouponCode = ""
                            newImageUrl = ""
                            showAddOfferDialog = false
                        } else {
                            Toast.makeText(context, "Preencha o título e o link de afiliado.", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Publicar Oferta")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddOfferDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Product Request Dialog
    if (showRequestDialog) {
        AlertDialog(
            onDismissRequest = { showRequestDialog = false },
            title = { Text("Pedir Produto com Desconto", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Qual produto você gostaria de encontrar em promoção nas próximas ofertas?",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = requestedProductName,
                        onValueChange = { requestedProductName = it },
                        label = { Text("Nome do produto ou marca") },
                        placeholder = { Text("Ex: Tênis Nike, Perfume X, Fone Bluetooth...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (requestedProductName.isNotBlank()) {
                            viewModel.submitProductRequest(requestedProductName, selectedCategoryFilter, "Plantonista")
                            Toast.makeText(context, "Pedido enviado com sucesso! Obrigado pela sugestão.", Toast.LENGTH_LONG).show()
                            requestedProductName = ""
                            showRequestDialog = false
                        }
                    }
                ) {
                    Text("Enviar Pedido")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRequestDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun OfferCardItemDedicated(
    offer: OfferItem,
    isAdminLoggedIn: Boolean,
    onDelete: () -> Unit,
    onCopyCoupon: (String) -> Unit,
    onOpenUrl: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Optional Image Banner if imageUrl is provided
            if (offer.imageUrl.isNotBlank()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.Image, contentDescription = null, modifier = Modifier.size(36.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Foto do Produto Vinculada", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = offer.marketplace,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = offer.category,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Text(
                            text = offer.discountBadge,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (isAdminLoggedIn) {
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Filled.Delete, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            Text(
                text = offer.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = offer.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = offer.priceTag,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                if (!offer.couponCode.isNullOrBlank()) {
                    OutlinedButton(
                        onClick = { onCopyCoupon(offer.couponCode) },
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cupom: ${offer.couponCode}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Button(
                onClick = { onOpenUrl(offer.affiliateUrl) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Ir para a Oferta / Resgatar Desconto")
            }
        }
    }
}
