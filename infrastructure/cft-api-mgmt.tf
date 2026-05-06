locals {
  cft_api_mgmt_suffix = var.apim_suffix == "" ? var.env : var.apim_suffix
  cft_api_mgmt_name   = join("-", ["cft-api-mgmt", local.cft_api_mgmt_suffix])
  cft_api_mgmt_rg     = join("-", ["cft", var.env, "network-rg"])
  cft_api_base_path   = "ia-api"
}

provider "azurerm" {
  alias           = "aks-cftapps"
  subscription_id = var.aks_subscription_id
  features {}
}

data "template_file" "cft_policy_template" {
  template = file("${path.module}/template/cft-api-policy.xml")

  vars = {
    allowed_certificate_thumbprints = local.thumbprints_in_quotes_str
    s2s_base_url                    = local.s2sUrl
  }

  depends_on = [
    resource.azurerm_api_management_named_value.ia_s2s_client_secret,
    resource.azurerm_api_management_named_value.ia_s2s_client_id
  ]
}

resource "azurerm_api_management_named_value" "ia_s2s_client_secret" {
  name                = "ia-s2s-client-secret"
  resource_group_name = local.cft_api_mgmt_rg
  api_management_name = local.cft_api_mgmt_name
  display_name        = "ia-s2s-client-secret"
  value               = data.azurerm_key_vault_secret.s2s_client_secret.value
  secret              = true
  provider            = azurerm.aks-cftapps

  depends_on = [
    module.cft_api_mgmt_api
  ]
}

resource "azurerm_api_management_named_value" "ia_s2s_client_id" {
  name                = "ia-s2s-client-id"
  resource_group_name = local.cft_api_mgmt_rg
  api_management_name = local.cft_api_mgmt_name
  display_name        = "ia-s2s-client-id"
  value               = data.azurerm_key_vault_secret.s2s_client_id.value
  secret              = false
  provider            = azurerm.aks-cftapps

  depends_on = [
    module.cft_api_mgmt_api
  ]
}

module "cft_api_mgmt_product" {
  source        = "git@github.com:hmcts/cnp-module-api-mgmt-product?ref=master"
  name          = var.product_name
  api_mgmt_name = local.cft_api_mgmt_name
  api_mgmt_rg   = local.cft_api_mgmt_rg
  providers = {
    azurerm = azurerm.aks-cftapps
  }
}

module "cft_api_mgmt_api" {
  source        = "git@github.com:hmcts/cnp-module-api-mgmt-api?ref=master"
  name          = join("-", [var.product_name, "api"])
  display_name  = "IA API"
  api_mgmt_name = local.cft_api_mgmt_name
  api_mgmt_rg   = local.cft_api_mgmt_rg
  product_id    = module.cft_api_mgmt_product.product_id
  path          = local.cft_api_base_path
  service_url   = local.ia_api_url
  swagger_url   = "https://raw.githubusercontent.com/hmcts/reform-api-docs/master/docs/specs/ccpay-payment-app.recon-payments-v0.3.json"
  protocols     = ["http", "https"]
  revision      = "1"
  providers = {
    azurerm = azurerm.aks-cftapps
  }
}

module "cft_api_mgmt_policy" {
  source                 = "git@github.com:hmcts/cnp-module-api-mgmt-api-policy?ref=master"
  api_mgmt_name          = local.cft_api_mgmt_name
  api_mgmt_rg            = local.cft_api_mgmt_rg
  api_name               = module.cft_api_mgmt_api.name
  api_policy_xml_content = data.template_file.cft_policy_template.rendered
  providers = {
    azurerm = azurerm.aks-cftapps
  }
}
