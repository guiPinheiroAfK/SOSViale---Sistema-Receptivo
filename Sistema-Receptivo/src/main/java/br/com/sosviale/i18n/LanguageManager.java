package br.com.sosviale.i18n;

import br.com.sosviale.service.StatusTransfer;

import java.util.*;

/*
 * LanguageManager - Gerenciador centralizado de idiomas
 * Suporta: Português (PT), Inglês (EN) e Espanhol (ES)
 */
public class LanguageManager {

    public enum Language {
        PORTUGUESE("pt_BR"),
        ENGLISH("en_US"),
        SPANISH("es_ES");

        private final String code;

        Language(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }

    private static LanguageManager instance;
    private Language currentLanguage;
    private final Map<Language, Map<String, String>> translations;
    private final List<LanguageChangeListener> listeners;

    private LanguageManager() {
        this.currentLanguage = Language.PORTUGUESE;
        this.translations = new HashMap<>();
        this.listeners = new ArrayList<>();
        loadTranslations();
    }

    public static synchronized LanguageManager getInstance() {
        if (instance == null) {
            instance = new LanguageManager();
        }
        return instance;
    }

    private void loadTranslations() {
        // Português
        Map<String, String> pt = new HashMap<>();
        pt.put("app.title", "SOS VIALE | Sistema Receptivo");
        pt.put("app.search.placeholder", "Buscar transfer, passageiro ou OS...");
        pt.put("button.logout", "Sair");
        pt.put("menu.modules", "MÓDULOS");
        pt.put("menu.admin", "ADMIN");
        pt.put("menu.dashboard", "📊 Painel Inicial");
        pt.put("menu.dashboard.subtitle", "Resumo operacional do dia");
        pt.put("menu.transfers", "🚗 Transfers");
        pt.put("menu.transfers.subtitle", "Cadastro e acompanhamento");
        pt.put("menu.passengers", "👥 Passageiros");
        pt.put("menu.passengers.subtitle", "Cadastro de passageiros");
        pt.put("menu.drivers", "🧑‍✈️ Motoristas");
        pt.put("menu.drivers.subtitle", "Gestão de motoristas");
        pt.put("menu.vehicles", "🚙 Veículos");
        pt.put("menu.vehicles.subtitle", "Controle da frota");
        pt.put("menu.orders", "📋 Ordens de Serviço");
        pt.put("menu.orders.subtitle", "Montagem de OS");
        pt.put("menu.users", "⚙️ Usuários");
        pt.put("menu.users.subtitle", "Gestão de usuários do sistema");
        pt.put("menu.pontosColeta", "📍 Pontos de Coleta");
        pt.put("menu.pontosColeta.subtitle", "Gerenciar pontos de parada por transfer");
        pt.put("dialog.confirm.logout", "Tem certeza que deseja sair?");
        pt.put("dialog.confirm.title", "Confirmação");
        pt.put("dialog.info.title", "SOS VIALE");
        pt.put("language.pt", "Português");
        pt.put("language.en", "English");
        pt.put("language.es", "Español");
        pt.put("language.label", "Idioma");
        pt.put("version", "v2.0 Refatorado");
        pt.put("login.title", "SOS VIALE");
        pt.put("login.subtitle", "Sistema Receptivo Trinacional");
        pt.put("login.username.label", "Usuário:");
        pt.put("login.password.label", "Senha:");
        pt.put("login.button.login", "Entrar");
        pt.put("login.button.register", "Registrar");
        pt.put("login.link.register", "Não tem conta? Registre-se");
        pt.put("login.link.login", "Já tem conta? Faça login");
        pt.put("register.title", "Criar Conta");
        pt.put("register.subtitle", "Cadastre um novo usuário para acessar o sistema.");
        pt.put("register.username.label", "Usuário:");
        pt.put("register.email.label", "Email:");
        pt.put("register.password.label", "Senha:");
        pt.put("register.confirm.label", "Confirmar Senha:");
        pt.put("register.button.register", "Criar Conta");
        pt.put("register.button.cancel", "Cancelar");
        pt.put("error.invalid.credentials", "Usuário ou senha inválidos");
        pt.put("error.user.exists", "Usuário já existe");
        pt.put("error.passwords.mismatch", "As senhas não correspondem");
        pt.put("error.required.fields", "Todos os campos são obrigatórios");

        // Dashboard Panel
        pt.put("dashboard.metric.passengers", "Passageiros cadastrados");
        pt.put("dashboard.metric.drivers", "Motoristas cadastrados");
        pt.put("dashboard.metric.vehicles", "Veículos cadastrados");
        pt.put("dashboard.metric.transfers", "Transfers sem OS");
        pt.put("dashboard.metric.passengers.hint", "no sistema");
        pt.put("dashboard.metric.drivers.hint", "no sistema");
        pt.put("dashboard.metric.vehicles.hint", "na frota");
        pt.put("dashboard.metric.transfers.hint", "aguardando vinculação");
        pt.put("dashboard.workflow.title", "Fluxo Operacional");
        pt.put("dashboard.workflow.step1", "Cadastrar passageiro e documentos");
        pt.put("dashboard.workflow.step2", "Agendar transfer com origem/destino");
        pt.put("dashboard.workflow.step3", "Vincular motorista e veículo em OS");
        pt.put("dashboard.workflow.step4", "Acompanhar status durante rota");
        pt.put("dashboard.workflow.step5", "Concluir OS e emitir PDF");

        // Transfers Panel
        pt.put("transfers.title", "Agendar Transfer");
        pt.put("transfers.label.origin", "Origem:");
        pt.put("transfers.label.destiny", "Destino:");
        pt.put("transfers.label.date", "Data (dd/MM/yyyy):");
        pt.put("transfers.label.time", "Hora (HH:mm):");
        pt.put("transfers.button.schedule", "Agendar");
        pt.put("transfers.button.delete", "Excluir");
        pt.put("transfers.button.clear", "Limpar");
        pt.put("transfers.list.title", "Transfers cadastrados");
        pt.put("transfers.table.id", "ID");
        pt.put("transfers.table.origin", "Origem");
        pt.put("transfers.table.destiny", "Destino");
        pt.put("transfers.table.date", "Data");
        pt.put("transfers.table.time", "Hora");
        pt.put("transfers.table.status", "Status");
        pt.put("transfers.table.driver", "Motorista");
        pt.put("transfers.table.value", "Valor (R$)");
        pt.put("transfers.button.add.passenger", "+ Adicionar Passageiro");
        pt.put("transfers.button.edit", "Salvar alteração");
        pt.put("transfers.message.required", "Preencha todos os campos!");
        pt.put("transfers.message.invalid", "Formato inválido!\nData: dd/MM/yyyy\nHora: HH:mm");
        pt.put("transfers.message.scheduled", "Transfer agendado!");
        pt.put("transfers.message.updated", "Transfer atualizado!");
        pt.put("transfers.message.deleted", "Transfer excluído!");
        pt.put("transfers.message.delete.confirm", "Tem certeza que deseja excluir este transfer?");
        pt.put("transfers.message.delete.confirm.title", "Confirmar exclusão");
        pt.put("transfers.message.error", "Erro");
        pt.put("transfers.message.success", "Sucesso");
        pt.put("transfers.message.warning", "Aviso");
        pt.put("transfers.status.no.os", "Sem OS");

        // Passengers Panel
        pt.put("passengers.title", "Cadastrar Passageiro");
        pt.put("passengers.label.name", "Nome:");
        pt.put("passengers.label.fullname", "Nome completo:");
        pt.put("passengers.label.document.number", "Número do Documento:");
        pt.put("passengers.label.nationality", "Nacionalidade:");
        pt.put("passengers.button.add", "Adicionar");
        pt.put("passengers.table.hint", "💡 Clique em um passageiro para editar ou excluir.");
        pt.put("passengers.table.document.type", "Tipo");
        pt.put("passengers.table.nationality", "Nacionalidade");
        pt.put("passengers.label.email", "Email:");
        pt.put("passengers.label.phone", "Telefone:");
        pt.put("passengers.label.document", "Documento:");
        pt.put("passengers.label.document.type", "Tipo:");
        pt.put("passengers.button.save", "Salvar");
        pt.put("passengers.button.delete", "Excluir");
        pt.put("passengers.button.clear", "Limpar");
        pt.put("passengers.list.title", "Passageiros cadastrados");
        pt.put("passengers.table.id", "ID");
        pt.put("passengers.table.name", "Nome");
        pt.put("passengers.table.email", "Email");
        pt.put("passengers.table.phone", "Telefone");
        pt.put("passengers.table.document", "Documento");

        // Drivers Panel
        pt.put("drivers.title", "Cadastrar Motorista");
        pt.put("drivers.label.name", "Nome:");
        pt.put("drivers.label.license", "CNH:");
        pt.put("drivers.label.phone", "Telefone:");
        pt.put("drivers.button.save", "Salvar");
        pt.put("drivers.button.delete", "Excluir");
        pt.put("drivers.button.clear", "Limpar");
        pt.put("drivers.list.title", "Motoristas cadastrados");
        pt.put("drivers.table.id", "ID");
        pt.put("drivers.table.name", "Nome");
        pt.put("drivers.table.license", "CNH");
        pt.put("drivers.table.phone", "Telefone");

        // Vehicles Panel
        pt.put("vehicles.title", "Cadastrar Veículo");
        pt.put("vehicles.label.model", "Modelo:");
        pt.put("vehicles.label.plate", "Placa:");
        pt.put("vehicles.label.capacity", "Capacidade:");
        pt.put("vehicles.button.save", "Salvar");
        pt.put("vehicles.button.delete", "Excluir");
        pt.put("vehicles.button.clear", "Limpar");
        pt.put("vehicles.list.title", "Veículos cadastrados");
        pt.put("vehicles.table.id", "ID");
        pt.put("vehicles.table.model", "Modelo");
        pt.put("vehicles.table.plate", "Placa");
        pt.put("vehicles.table.capacity", "Capacidade");

        // Orders Panel
        pt.put("orders.title", "Montar Ordem de Serviço");
        pt.put("orders.label.transfer", "Transfer:");
        pt.put("orders.label.driver", "Motorista:");
        pt.put("orders.label.vehicle", "Veículo:");
        pt.put("orders.button.create", "Criar OS");
        pt.put("orders.button.delete", "Excluir");
        pt.put("orders.button.pdf", "Gerar PDF");
        pt.put("orders.list.title", "Ordens de Serviço");
        pt.put("orders.table.id", "ID");
        pt.put("orders.table.transfer", "Transfer");
        pt.put("orders.table.driver", "Motorista");
        pt.put("orders.table.vehicle", "Veículo");
        pt.put("orders.table.status", "Status");

        // Pontos Coleta Panel
        pt.put("collection.title", "Cadastrar Ponto de Coleta");
        pt.put("collection.form.title", "Cadastro de Local");
        pt.put("collection.table.hint", "💡 Clique em um local para editar ou excluir.");
        pt.put("collection.label.name", "Nome:");
        pt.put("collection.label.address", "Endereço:");
        pt.put("collection.label.city", "Cidade:");
        pt.put("collection.button.save", "Salvar");
        pt.put("collection.button.delete", "Excluir");
        pt.put("collection.button.clear", "Limpar");
        pt.put("collection.list.title", "Pontos de Coleta cadastrados");
        pt.put("collection.table.id", "ID");
        pt.put("collection.table.name", "Nome");
        pt.put("collection.table.address", "Endereço");
        pt.put("collection.table.city", "Cidade");

        pt.put("menu.servicos", "🛠️ Serviços");
        pt.put("menu.servicos.subtitle", "Transfers vinculados à OS — motorista");
        pt.put("nav.section.gerente", "GERENTE");
        pt.put("nav.section.motorista", "MOTORISTA");
        pt.put("nav.section.admin", "ADMIN");
        pt.put("access.denied", "Você não tem permissão para acessar.");
        pt.put("access.denied.title", "Acesso negado");
        pt.put("notifications.bell", "🔔");
        pt.put("notifications.tooltip", "Notificações de transfers");
        pt.put("notifications.title", "Notificações");
        pt.put("notifications.empty", "Nenhuma notificação no momento.");
        pt.put("notifications.dismiss", "Remover");
        pt.put("notifications.clear.all", "Limpar todas");
        pt.put("notifications.transfer.alert",
                "Em {minutos} min será o horário do Transfer #{id}\n{origem} → {destino} às {hora}");
        pt.put("status.transfer.AGUARDANDO_OS", "Aguardando OS");
        pt.put("status.transfer.NA_OS", "Na OS");
        pt.put("status.transfer.EM_EXECUCAO", "Em Execução");
        pt.put("status.transfer.CONCLUIDO", "Concluído");
        pt.put("status.transfer.CANCELADO", "Cancelado");
        pt.put("dashboard.welcome", "Painel operacional");
        pt.put("dashboard.today", "Hoje");
        pt.put("dashboard.metric.orders", "Ordens de serviço");
        pt.put("dashboard.metric.orders.hint", "cadastradas");
        pt.put("dashboard.metric.today", "Transfers hoje");
        pt.put("dashboard.metric.today.hint", "agendados para hoje");
        pt.put("dashboard.metric.linked", "Transfers na OS");
        pt.put("dashboard.metric.linked.hint", "vinculados a rotas");
        pt.put("dashboard.metric.execution", "Em execução");
        pt.put("dashboard.metric.execution.hint", "em andamento agora");
        pt.put("dashboard.upcoming.title", "Próximos transfers");
        pt.put("dashboard.upcoming.col.id", "ID");
        pt.put("dashboard.upcoming.col.route", "Rota");
        pt.put("dashboard.upcoming.col.datetime", "Data / Hora");
        pt.put("dashboard.upcoming.col.status", "Status");
        pt.put("dashboard.upcoming.empty", "Nenhum transfer futuro agendado.");
        pt.put("dashboard.error.load", "Não foi possível carregar os dados do painel.");
        pt.put("common.error.title", "Erro");
        pt.put("servicos.form.title", "Detalhes do Serviço");
        pt.put("servicos.label.transfer", "Transfer #:");
        pt.put("servicos.label.os", "Ordem de serviço:");
        pt.put("servicos.label.driver", "Motorista:");
        pt.put("servicos.label.route", "Rota:");
        pt.put("servicos.label.passengers", "Passageiros:");
        pt.put("servicos.label.status", "Status:");
        pt.put("servicos.table.title", "Serviços ativos (transfers na OS)");
        pt.put("servicos.table.hint", "💡 Selecione um serviço da OS para alterar status, baixar PDF da OS ou excluir o transfer.");
        pt.put("servicos.button.pdf", "Baixar PDF da OS");
        pt.put("servicos.button.delete", "Excluir serviço");
        pt.put("servicos.table.col.id", "ID");
        pt.put("servicos.table.col.os", "OS");
        pt.put("servicos.table.col.driver", "Motorista");
        pt.put("servicos.table.col.route", "Origem / Destino");
        pt.put("servicos.table.col.pax", "Pax");
        pt.put("servicos.table.col.status", "Status");
        pt.put("login.access.title", "Acesso ao Sistema");
        pt.put("login.access.subtitle", "Entre com seu usuário e senha para continuar.");
        pt.put("login.create.account", "Criar conta");
        pt.put("ordem.title", "Montagem de OS — criar, vincular transfers e otimizar rota");
        pt.put("users.title", "Cadastro de Usuário");
        pt.put("users.list.title", "Usuários do sistema");
        pt.put("passengers.form.title", "Cadastro de Passageiro");
        pt.put("drivers.form.title", "Cadastro de Motorista");
        pt.put("drivers.label.fullname", "Nome completo:");
        pt.put("vehicles.form.title", "Cadastro de Veículo");
        pt.put("common.save", "Salvar");
        pt.put("common.delete", "Excluir");
        pt.put("common.clear", "Limpar");
        pt.put("common.na", "N/D");
        pt.put("servicos.warn.select.os", "Selecione um serviço vinculado a uma OS.");
        pt.put("servicos.error.os.notfound", "OS não encontrada.");
        pt.put("servicos.warn.no.transfers", "A OS não possui transfers vinculados para gerar o PDF.");
        pt.put("servicos.pdf.saved", "PDF salvo em:\n{path}\n\nDeseja abrir o arquivo?");
        pt.put("servicos.pdf.title", "PDF gerado");
        pt.put("servicos.pdf.error", "Erro ao gerar PDF: {msg}");
        pt.put("servicos.error.transfer.notfound", "Transfer não encontrado.");
        pt.put("servicos.status.updated", "Status atualizado para: {status}");
        pt.put("servicos.error.update", "Erro ao atualizar: {msg}");
        pt.put("servicos.delete.confirm", "Excluir o transfer #{id} do banco de dados?\nEsta ação não pode ser desfeita.");
        pt.put("servicos.delete.success", "Serviço excluído do banco.");
        pt.put("servicos.error.delete", "Erro ao excluir: {msg}");
        pt.put("login.button.offline", "Entrar offline (dados salvos)");
        pt.put("login.offline.need.user", "Informe o usuário para carregar o cache local.");
        pt.put("login.offline.no.cache", "Não há dados offline para este usuário. Conecte-se na primeira vez.");
        pt.put("login.offline.no.network", "Sem rede — tentando modo offline...");
        pt.put("offline.status.online", "🟢 Online — dados sincronizados");
        pt.put("offline.status.offline", "🟠 Modo offline — consultando dados do dispositivo");
        pt.put("offline.status.empty", "⚠ Nenhum dado local. Conecte-se e abra Serviços uma vez.");
        pt.put("offline.status.synced", "última sync: {when}");
        pt.put("offline.status.pending", "{n} alteração(ões) aguardando envio");

        // Inglês
        Map<String, String> en = new HashMap<>();
        en.put("app.title", "SOS VIALE | Receptive System");
        en.put("app.search.placeholder", "Search transfer, passenger or OS...");
        en.put("button.logout", "Logout");
        en.put("menu.modules", "MODULES");
        en.put("menu.admin", "ADMIN");
        en.put("menu.dashboard", "📊 Dashboard");
        en.put("menu.dashboard.subtitle", "Daily operational summary");
        en.put("menu.transfers", "🚗 Transfers");
        en.put("menu.transfers.subtitle", "Registration and tracking");
        en.put("menu.passengers", "👥 Passengers");
        en.put("menu.passengers.subtitle", "Passenger registration");
        en.put("menu.drivers", "🧑‍✈️ Drivers");
        en.put("menu.drivers.subtitle", "Driver management");
        en.put("menu.vehicles", "🚙 Vehicles");
        en.put("menu.vehicles.subtitle", "Fleet control");
        en.put("menu.orders", "📋 Service Orders");
        en.put("menu.orders.subtitle", "Service order assembly");
        en.put("menu.users", "⚙️ Users");
        en.put("menu.users.subtitle", "System user management");
        en.put("menu.pontosColeta", "📍 Collection Points");
        en.put("menu.pontosColeta.subtitle", "Manage stop points per transfer");
        en.put("dialog.confirm.logout", "Are you sure you want to logout?");
        en.put("dialog.confirm.title", "Confirmation");
        en.put("dialog.info.title", "SOS VIALE");
        en.put("language.pt", "Português");
        en.put("language.en", "English");
        en.put("language.es", "Español");
        en.put("language.label", "Language");
        en.put("version", "v2.0 Refactored");
        en.put("login.title", "SOS VIALE");
        en.put("login.subtitle", "Trinational Receptive System");
        en.put("login.username.label", "Username:");
        en.put("login.password.label", "Password:");
        en.put("login.button.login", "Login");
        en.put("login.button.register", "Register");
        en.put("login.link.register", "Don't have an account? Register");
        en.put("login.link.login", "Already have an account? Login");
        en.put("register.title", "Create Account");
        en.put("register.subtitle", "Register a new user to access the system.");
        en.put("register.username.label", "Username:");
        en.put("register.email.label", "Email:");
        en.put("register.password.label", "Password:");
        en.put("register.confirm.label", "Confirm Password:");
        en.put("register.button.register", "Create Account");
        en.put("register.button.cancel", "Cancel");
        en.put("error.invalid.credentials", "Invalid username or password");
        en.put("error.user.exists", "User already exists");
        en.put("error.passwords.mismatch", "Passwords do not match");
        en.put("error.required.fields", "All fields are required");

        // Dashboard Panel
        en.put("dashboard.metric.passengers", "Registered Passengers");
        en.put("dashboard.metric.drivers", "Registered Drivers");
        en.put("dashboard.metric.vehicles", "Registered Vehicles");
        en.put("dashboard.metric.transfers", "Transfers without OS");
        en.put("dashboard.metric.passengers.hint", "in the system");
        en.put("dashboard.metric.drivers.hint", "in the system");
        en.put("dashboard.metric.vehicles.hint", "in the fleet");
        en.put("dashboard.metric.transfers.hint", "awaiting linking");
        en.put("dashboard.workflow.title", "Operational Flow");
        en.put("dashboard.workflow.step1", "Register passenger and documents");
        en.put("dashboard.workflow.step2", "Schedule transfer with origin/destination");
        en.put("dashboard.workflow.step3", "Link driver and vehicle in OS");
        en.put("dashboard.workflow.step4", "Track status during route");
        en.put("dashboard.workflow.step5", "Complete OS and generate PDF");

        // Transfers Panel
        en.put("transfers.title", "Schedule Transfer");
        en.put("transfers.label.origin", "Origin:");
        en.put("transfers.label.destiny", "Destination:");
        en.put("transfers.label.date", "Date (dd/MM/yyyy):");
        en.put("transfers.label.time", "Time (HH:mm):");
        en.put("transfers.button.schedule", "Schedule");
        en.put("transfers.button.delete", "Delete");
        en.put("transfers.button.clear", "Clear");
        en.put("transfers.list.title", "Registered Transfers");
        en.put("transfers.table.id", "ID");
        en.put("transfers.table.origin", "Origin");
        en.put("transfers.table.destiny", "Destination");
        en.put("transfers.table.date", "Date");
        en.put("transfers.table.time", "Time");
        en.put("transfers.table.status", "Status");
        en.put("transfers.table.driver", "Driver");
        en.put("transfers.table.value", "Amount (BRL)");
        en.put("transfers.button.add.passenger", "+ Add Passenger");
        en.put("transfers.button.edit", "Save changes");
        en.put("transfers.message.required", "Fill in all fields!");
        en.put("transfers.message.invalid", "Invalid format!\nDate: dd/MM/yyyy\nTime: HH:mm");
        en.put("transfers.message.scheduled", "Transfer scheduled!");
        en.put("transfers.message.updated", "Transfer updated!");
        en.put("transfers.message.deleted", "Transfer deleted!");
        en.put("transfers.message.delete.confirm", "Are you sure you want to delete this transfer?");
        en.put("transfers.message.delete.confirm.title", "Confirm deletion");
        en.put("transfers.message.error", "Error");
        en.put("transfers.message.success", "Success");
        en.put("transfers.message.warning", "Warning");
        en.put("transfers.status.no.os", "No OS");

        // Passengers Panel
        en.put("passengers.title", "Register Passenger");
        en.put("passengers.label.name", "Name:");
        en.put("passengers.label.fullname", "Full name:");
        en.put("passengers.label.document.number", "Document number:");
        en.put("passengers.label.nationality", "Nationality:");
        en.put("passengers.button.add", "Add");
        en.put("passengers.table.hint", "💡 Click a passenger to edit or delete.");
        en.put("passengers.table.document.type", "Type");
        en.put("passengers.table.nationality", "Nationality");
        en.put("passengers.label.email", "Email:");
        en.put("passengers.label.phone", "Phone:");
        en.put("passengers.label.document", "Document:");
        en.put("passengers.label.document.type", "Type:");
        en.put("passengers.button.save", "Save");
        en.put("passengers.button.delete", "Delete");
        en.put("passengers.button.clear", "Clear");
        en.put("passengers.list.title", "Registered Passengers");
        en.put("passengers.table.id", "ID");
        en.put("passengers.table.name", "Name");
        en.put("passengers.table.email", "Email");
        en.put("passengers.table.phone", "Phone");
        en.put("passengers.table.document", "Document");

        // Drivers Panel
        en.put("drivers.title", "Register Driver");
        en.put("drivers.label.name", "Name:");
        en.put("drivers.label.license", "License:");
        en.put("drivers.label.phone", "Phone:");
        en.put("drivers.button.save", "Save");
        en.put("drivers.button.delete", "Delete");
        en.put("drivers.button.clear", "Clear");
        en.put("drivers.list.title", "Registered Drivers");
        en.put("drivers.table.id", "ID");
        en.put("drivers.table.name", "Name");
        en.put("drivers.table.license", "License");
        en.put("drivers.table.phone", "Phone");

        // Vehicles Panel
        en.put("vehicles.title", "Register Vehicle");
        en.put("vehicles.label.model", "Model:");
        en.put("vehicles.label.plate", "License Plate:");
        en.put("vehicles.label.capacity", "Capacity:");
        en.put("vehicles.button.save", "Save");
        en.put("vehicles.button.delete", "Delete");
        en.put("vehicles.button.clear", "Clear");
        en.put("vehicles.list.title", "Registered Vehicles");
        en.put("vehicles.table.id", "ID");
        en.put("vehicles.table.model", "Model");
        en.put("vehicles.table.plate", "License Plate");
        en.put("vehicles.table.capacity", "Capacity");

        // Orders Panel
        en.put("orders.title", "Create Service Order");
        en.put("orders.label.transfer", "Transfer:");
        en.put("orders.label.driver", "Driver:");
        en.put("orders.label.vehicle", "Vehicle:");
        en.put("orders.button.create", "Create OS");
        en.put("orders.button.delete", "Delete");
        en.put("orders.button.pdf", "Generate PDF");
        en.put("orders.list.title", "Service Orders");
        en.put("orders.table.id", "ID");
        en.put("orders.table.transfer", "Transfer");
        en.put("orders.table.driver", "Driver");
        en.put("orders.table.vehicle", "Vehicle");
        en.put("orders.table.status", "Status");

        // Pontos Coleta Panel
        en.put("collection.title", "Register Collection Point");
        en.put("collection.form.title", "Location registration");
        en.put("collection.table.hint", "💡 Click a location to edit or delete.");
        en.put("collection.label.name", "Name:");
        en.put("collection.label.address", "Address:");
        en.put("collection.label.city", "City:");
        en.put("collection.button.save", "Save");
        en.put("collection.button.delete", "Delete");
        en.put("collection.button.clear", "Clear");
        en.put("collection.list.title", "Registered Collection Points");
        en.put("collection.table.id", "ID");
        en.put("collection.table.name", "Name");
        en.put("collection.table.address", "Address");
        en.put("collection.table.city", "City");

        en.put("menu.servicos", "🛠️ Services");
        en.put("menu.servicos.subtitle", "Transfers linked to service orders — driver");
        en.put("nav.section.gerente", "MANAGER");
        en.put("nav.section.motorista", "DRIVER");
        en.put("nav.section.admin", "ADMIN");
        en.put("access.denied", "You do not have permission to access this.");
        en.put("access.denied.title", "Access denied");
        en.put("notifications.bell", "🔔");
        en.put("notifications.tooltip", "Transfer notifications");
        en.put("notifications.title", "Notifications");
        en.put("notifications.empty", "No notifications at the moment.");
        en.put("notifications.dismiss", "Dismiss");
        en.put("notifications.clear.all", "Clear all");
        en.put("notifications.transfer.alert",
                "Transfer #{id} is scheduled in {minutos} min\n{origem} → {destino} at {hora}");
        en.put("status.transfer.AGUARDANDO_OS", "Awaiting SO");
        en.put("status.transfer.NA_OS", "On SO");
        en.put("status.transfer.EM_EXECUCAO", "In progress");
        en.put("status.transfer.CONCLUIDO", "Completed");
        en.put("status.transfer.CANCELADO", "Cancelled");
        en.put("dashboard.welcome", "Operations dashboard");
        en.put("dashboard.today", "Today");
        en.put("dashboard.metric.orders", "Service orders");
        en.put("dashboard.metric.orders.hint", "registered");
        en.put("dashboard.metric.today", "Transfers today");
        en.put("dashboard.metric.today.hint", "scheduled for today");
        en.put("dashboard.metric.linked", "Transfers on SO");
        en.put("dashboard.metric.linked.hint", "linked to routes");
        en.put("dashboard.metric.execution", "In execution");
        en.put("dashboard.metric.execution.hint", "currently running");
        en.put("dashboard.upcoming.title", "Upcoming transfers");
        en.put("dashboard.upcoming.col.id", "ID");
        en.put("dashboard.upcoming.col.route", "Route");
        en.put("dashboard.upcoming.col.datetime", "Date / Time");
        en.put("dashboard.upcoming.col.status", "Status");
        en.put("dashboard.upcoming.empty", "No upcoming transfers scheduled.");
        en.put("dashboard.error.load", "Could not load dashboard data.");
        en.put("common.error.title", "Error");
        en.put("servicos.form.title", "Service details");
        en.put("servicos.label.transfer", "Transfer #:");
        en.put("servicos.label.os", "Service order:");
        en.put("servicos.label.driver", "Driver:");
        en.put("servicos.label.route", "Route:");
        en.put("servicos.label.passengers", "Passengers:");
        en.put("servicos.label.status", "Status:");
        en.put("servicos.table.title", "Active services (transfers on SO)");
        en.put("servicos.table.hint", "💡 Select a service to change status, download SO PDF or delete the transfer.");
        en.put("servicos.button.pdf", "Download SO PDF");
        en.put("servicos.button.delete", "Delete service");
        en.put("servicos.table.col.id", "ID");
        en.put("servicos.table.col.os", "SO");
        en.put("servicos.table.col.driver", "Driver");
        en.put("servicos.table.col.route", "Origin / Destination");
        en.put("servicos.table.col.pax", "Pax");
        en.put("servicos.table.col.status", "Status");
        en.put("login.access.title", "System access");
        en.put("login.access.subtitle", "Enter your username and password to continue.");
        en.put("login.create.account", "Create account");
        en.put("ordem.title", "Build SO — create, link transfers and optimize route");
        en.put("users.title", "User registration");
        en.put("users.list.title", "System users");
        en.put("passengers.form.title", "Passenger registration");
        en.put("drivers.form.title", "Driver registration");
        en.put("drivers.label.fullname", "Full name:");
        en.put("vehicles.form.title", "Vehicle registration");
        en.put("common.save", "Save");
        en.put("common.delete", "Delete");
        en.put("common.clear", "Clear");
        en.put("common.na", "N/A");
        en.put("servicos.warn.select.os", "Select a service linked to a service order.");
        en.put("servicos.error.os.notfound", "Service order not found.");
        en.put("servicos.warn.no.transfers", "The service order has no linked transfers to generate the PDF.");
        en.put("servicos.pdf.saved", "PDF saved at:\n{path}\n\nOpen the file?");
        en.put("servicos.pdf.title", "PDF generated");
        en.put("servicos.pdf.error", "Error generating PDF: {msg}");
        en.put("servicos.error.transfer.notfound", "Transfer not found.");
        en.put("servicos.status.updated", "Status updated to: {status}");
        en.put("servicos.error.update", "Update error: {msg}");
        en.put("servicos.delete.confirm", "Delete transfer #{id} from the database?\nThis action cannot be undone.");
        en.put("servicos.delete.success", "Service deleted from database.");
        en.put("servicos.error.delete", "Delete error: {msg}");
        en.put("login.button.offline", "Offline login (saved data)");
        en.put("login.offline.need.user", "Enter username to load local cache.");
        en.put("login.offline.no.cache", "No offline data for this user. Connect online first.");
        en.put("login.offline.no.network", "No network — trying offline mode...");
        en.put("offline.status.online", "🟢 Online — data synced");
        en.put("offline.status.offline", "🟠 Offline mode — using device data");
        en.put("offline.status.empty", "⚠ No local data. Connect and open Services once.");
        en.put("offline.status.synced", "last sync: {when}");
        en.put("offline.status.pending", "{n} change(s) pending upload");

        // Espanhol
        Map<String, String> es = new HashMap<>();
        es.put("app.title", "SOS VIALE | Sistema Receptivo");
        es.put("app.search.placeholder", "Buscar transferencia, pasajero u OS...");
        es.put("button.logout", "Cerrar sesión");
        es.put("menu.modules", "MÓDULOS");
        es.put("menu.admin", "ADMINISTRADOR");
        es.put("menu.dashboard", "📊 Panel Principal");
        es.put("menu.dashboard.subtitle", "Resumen operacional del día");
        es.put("menu.transfers", "🚗 Transferencias");
        es.put("menu.transfers.subtitle", "Registro y seguimiento");
        es.put("menu.passengers", "👥 Pasajeros");
        es.put("menu.passengers.subtitle", "Registro de pasajeros");
        es.put("menu.drivers", "🧑‍✈️ Conductores");
        es.put("menu.drivers.subtitle", "Gestión de conductores");
        es.put("menu.vehicles", "🚙 Vehículos");
        es.put("menu.vehicles.subtitle", "Control de flota");
        es.put("menu.orders", "📋 Órdenes de Servicio");
        es.put("menu.orders.subtitle", "Montaje de órdenes");
        es.put("menu.users", "⚙️ Usuarios");
        es.put("menu.users.subtitle", "Gestión de usuarios del sistema");
        es.put("menu.pontosColeta", "📍 Puntos de Recogida");
        es.put("menu.pontosColeta.subtitle", "Gestionar puntos de parada por transferencia");
        es.put("dialog.confirm.logout", "¿Está seguro de que desea cerrar sesión?");
        es.put("dialog.confirm.title", "Confirmación");
        es.put("dialog.info.title", "SOS VIALE");
        es.put("language.pt", "Português");
        es.put("language.en", "English");
        es.put("language.es", "Español");
        es.put("language.label", "Idioma");
        es.put("version", "v2.0 Refactorizado");
        es.put("login.title", "SOS VIALE");
        es.put("login.subtitle", "Sistema Receptivo Trinacional");
        es.put("login.username.label", "Usuario:");
        es.put("login.password.label", "Contraseña:");
        es.put("login.button.login", "Iniciar Sesión");
        es.put("login.button.register", "Registrarse");
        es.put("login.link.register", "¿Sin cuenta? Regístrese");
        es.put("login.link.login", "¿Tiene cuenta? Inicie sesión");
        es.put("register.title", "Crear Cuenta");
        es.put("register.subtitle", "Registre un nuevo usuario para acceder al sistema.");
        es.put("register.username.label", "Usuario:");
        es.put("register.email.label", "Correo Electrónico:");
        es.put("register.password.label", "Contraseña:");
        es.put("register.confirm.label", "Confirmar Contraseña:");
        es.put("register.button.register", "Crear Cuenta");
        es.put("register.button.cancel", "Cancelar");
        es.put("error.invalid.credentials", "Usuario o contraseña inválidos");
        es.put("error.user.exists", "El usuario ya existe");
        es.put("error.passwords.mismatch", "Las contraseñas no coinciden");
        es.put("error.required.fields", "Todos los campos son obligatorios");

        // Dashboard Panel
        es.put("dashboard.metric.passengers", "Pasajeros Registrados");
        es.put("dashboard.metric.drivers", "Conductores Registrados");
        es.put("dashboard.metric.vehicles", "Vehículos Registrados");
        es.put("dashboard.metric.transfers", "Transferencias sin OS");
        es.put("dashboard.metric.passengers.hint", "en el sistema");
        es.put("dashboard.metric.drivers.hint", "en el sistema");
        es.put("dashboard.metric.vehicles.hint", "en la flota");
        es.put("dashboard.metric.transfers.hint", "esperando vinculación");
        es.put("dashboard.workflow.title", "Flujo Operacional");
        es.put("dashboard.workflow.step1", "Registrar pasajero y documentos");
        es.put("dashboard.workflow.step2", "Agendar transferencia con origen/destino");
        es.put("dashboard.workflow.step3", "Vincular conductor y vehículo en OS");
        es.put("dashboard.workflow.step4", "Seguimiento de estado durante la ruta");
        es.put("dashboard.workflow.step5", "Completar OS y generar PDF");

        // Transfers Panel
        es.put("transfers.title", "Agendar Transferencia");
        es.put("transfers.label.origin", "Origen:");
        es.put("transfers.label.destiny", "Destino:");
        es.put("transfers.label.date", "Fecha (dd/MM/yyyy):");
        es.put("transfers.label.time", "Hora (HH:mm):");
        es.put("transfers.button.schedule", "Agendar");
        es.put("transfers.button.delete", "Eliminar");
        es.put("transfers.button.clear", "Limpiar");
        es.put("transfers.list.title", "Transferencias Registradas");
        es.put("transfers.table.id", "ID");
        es.put("transfers.table.origin", "Origen");
        es.put("transfers.table.destiny", "Destino");
        es.put("transfers.table.date", "Fecha");
        es.put("transfers.table.time", "Hora");
        es.put("transfers.table.status", "Estado");
        es.put("transfers.table.driver", "Conductor");
        es.put("transfers.table.value", "Valor (BRL)");
        es.put("transfers.button.add.passenger", "+ Agregar Pasajero");
        es.put("transfers.button.edit", "Guardar cambios");
        es.put("transfers.message.required", "¡Rellene todos los campos!");
        es.put("transfers.message.invalid", "¡Formato inválido!\nFecha: dd/MM/yyyy\nHora: HH:mm");
        es.put("transfers.message.scheduled", "¡Transferencia agendada!");
        es.put("transfers.message.updated", "¡Transferencia actualizada!");
        es.put("transfers.message.deleted", "¡Transferencia eliminada!");
        es.put("transfers.message.delete.confirm", "¿Está seguro de que desea eliminar esta transferencia?");
        es.put("transfers.message.delete.confirm.title", "Confirmar eliminación");
        es.put("transfers.message.error", "Error");
        es.put("transfers.message.success", "Éxito");
        es.put("transfers.message.warning", "Advertencia");
        es.put("transfers.status.no.os", "Sin OS");

        // Passengers Panel
        es.put("passengers.title", "Registrar Pasajero");
        es.put("passengers.label.name", "Nombre:");
        es.put("passengers.label.fullname", "Nombre completo:");
        es.put("passengers.label.document.number", "Número de documento:");
        es.put("passengers.label.nationality", "Nacionalidad:");
        es.put("passengers.button.add", "Agregar");
        es.put("passengers.table.hint", "💡 Haga clic en un pasajero para editar o eliminar.");
        es.put("passengers.table.document.type", "Tipo");
        es.put("passengers.table.nationality", "Nacionalidad");
        es.put("passengers.label.email", "Correo Electrónico:");
        es.put("passengers.label.phone", "Teléfono:");
        es.put("passengers.label.document", "Documento:");
        es.put("passengers.label.document.type", "Tipo:");
        es.put("passengers.button.save", "Guardar");
        es.put("passengers.button.delete", "Eliminar");
        es.put("passengers.button.clear", "Limpiar");
        es.put("passengers.list.title", "Pasajeros Registrados");
        es.put("passengers.table.id", "ID");
        es.put("passengers.table.name", "Nombre");
        es.put("passengers.table.email", "Correo Electrónico");
        es.put("passengers.table.phone", "Teléfono");
        es.put("passengers.table.document", "Documento");

        // Drivers Panel
        es.put("drivers.title", "Registrar Conductor");
        es.put("drivers.label.name", "Nombre:");
        es.put("drivers.label.license", "Licencia:");
        es.put("drivers.label.phone", "Teléfono:");
        es.put("drivers.button.save", "Guardar");
        es.put("drivers.button.delete", "Eliminar");
        es.put("drivers.button.clear", "Limpiar");
        es.put("drivers.list.title", "Conductores Registrados");
        es.put("drivers.table.id", "ID");
        es.put("drivers.table.name", "Nombre");
        es.put("drivers.table.license", "Licencia");
        es.put("drivers.table.phone", "Teléfono");

        // Vehicles Panel
        es.put("vehicles.title", "Registrar Vehículo");
        es.put("vehicles.label.model", "Modelo:");
        es.put("vehicles.label.plate", "Placa:");
        es.put("vehicles.label.capacity", "Capacidad:");
        es.put("vehicles.button.save", "Guardar");
        es.put("vehicles.button.delete", "Eliminar");
        es.put("vehicles.button.clear", "Limpiar");
        es.put("vehicles.list.title", "Vehículos Registrados");
        es.put("vehicles.table.id", "ID");
        es.put("vehicles.table.model", "Modelo");
        es.put("vehicles.table.plate", "Placa");
        es.put("vehicles.table.capacity", "Capacidad");

        // Orders Panel
        es.put("orders.title", "Crear Orden de Servicio");
        es.put("orders.label.transfer", "Transferencia:");
        es.put("orders.label.driver", "Conductor:");
        es.put("orders.label.vehicle", "Vehículo:");
        es.put("orders.button.create", "Crear OS");
        es.put("orders.button.delete", "Eliminar");
        es.put("orders.button.pdf", "Generar PDF");
        es.put("orders.list.title", "Órdenes de Servicio");
        es.put("orders.table.id", "ID");
        es.put("orders.table.transfer", "Transferencia");
        es.put("orders.table.driver", "Conductor");
        es.put("orders.table.vehicle", "Vehículo");
        es.put("orders.table.status", "Estado");

        // Pontos Coleta Panel
        es.put("collection.title", "Registrar Punto de Recogida");
        es.put("collection.form.title", "Registro de local");
        es.put("collection.table.hint", "💡 Haga clic en un local para editar o eliminar.");
        es.put("collection.label.name", "Nombre:");
        es.put("collection.label.address", "Dirección:");
        es.put("collection.label.city", "Ciudad:");
        es.put("collection.button.save", "Guardar");
        es.put("collection.button.delete", "Eliminar");
        es.put("collection.button.clear", "Limpiar");
        es.put("collection.list.title", "Puntos de Recogida Registrados");
        es.put("collection.table.id", "ID");
        es.put("collection.table.name", "Nombre");
        es.put("collection.table.address", "Dirección");
        es.put("collection.table.city", "Ciudad");

        es.put("menu.servicos", "🛠️ Servicios");
        es.put("menu.servicos.subtitle", "Transferencias vinculadas a la OS — conductor");
        es.put("nav.section.gerente", "GERENTE");
        es.put("nav.section.motorista", "CONDUCTOR");
        es.put("nav.section.admin", "ADMIN");
        es.put("access.denied", "No tiene permiso para acceder.");
        es.put("access.denied.title", "Acceso denegado");
        es.put("notifications.bell", "🔔");
        es.put("notifications.tooltip", "Notificaciones de transferencias");
        es.put("notifications.title", "Notificaciones");
        es.put("notifications.empty", "No hay notificaciones en este momento.");
        es.put("notifications.dismiss", "Quitar");
        es.put("notifications.clear.all", "Limpiar todas");
        es.put("notifications.transfer.alert",
                "En {minutos} min será la hora del Transfer #{id}\n{origem} → {destino} a las {hora}");
        es.put("status.transfer.AGUARDANDO_OS", "Esperando OS");
        es.put("status.transfer.NA_OS", "En OS");
        es.put("status.transfer.EM_EXECUCAO", "En ejecución");
        es.put("status.transfer.CONCLUIDO", "Concluido");
        es.put("status.transfer.CANCELADO", "Cancelado");
        es.put("dashboard.welcome", "Panel operacional");
        es.put("dashboard.today", "Hoy");
        es.put("dashboard.metric.orders", "Órdenes de servicio");
        es.put("dashboard.metric.orders.hint", "registradas");
        es.put("dashboard.metric.today", "Transferencias hoy");
        es.put("dashboard.metric.today.hint", "agendadas para hoy");
        es.put("dashboard.metric.linked", "Transferencias en OS");
        es.put("dashboard.metric.linked.hint", "vinculadas a rutas");
        es.put("dashboard.metric.execution", "En ejecución");
        es.put("dashboard.metric.execution.hint", "en curso ahora");
        es.put("dashboard.upcoming.title", "Próximas transferencias");
        es.put("dashboard.upcoming.col.id", "ID");
        es.put("dashboard.upcoming.col.route", "Ruta");
        es.put("dashboard.upcoming.col.datetime", "Fecha / Hora");
        es.put("dashboard.upcoming.col.status", "Estado");
        es.put("dashboard.upcoming.empty", "No hay transferencias futuras agendadas.");
        es.put("dashboard.error.load", "No se pudieron cargar los datos del panel.");
        es.put("common.error.title", "Error");
        es.put("servicos.form.title", "Detalles del servicio");
        es.put("servicos.label.transfer", "Transfer #:");
        es.put("servicos.label.os", "Orden de servicio:");
        es.put("servicos.label.driver", "Conductor:");
        es.put("servicos.label.route", "Ruta:");
        es.put("servicos.label.passengers", "Pasajeros:");
        es.put("servicos.label.status", "Estado:");
        es.put("servicos.table.title", "Servicios activos (transferencias en OS)");
        es.put("servicos.table.hint", "💡 Seleccione un servicio para cambiar estado, descargar PDF de la OS o eliminar la transferencia.");
        es.put("servicos.button.pdf", "Descargar PDF de la OS");
        es.put("servicos.button.delete", "Eliminar servicio");
        es.put("servicos.table.col.id", "ID");
        es.put("servicos.table.col.os", "OS");
        es.put("servicos.table.col.driver", "Conductor");
        es.put("servicos.table.col.route", "Origen / Destino");
        es.put("servicos.table.col.pax", "Pax");
        es.put("servicos.table.col.status", "Estado");
        es.put("login.access.title", "Acceso al sistema");
        es.put("login.access.subtitle", "Ingrese su usuario y contraseña para continuar.");
        es.put("login.create.account", "Crear cuenta");
        es.put("ordem.title", "Montaje de OS — crear, vincular transferencias y optimizar ruta");
        es.put("users.title", "Registro de usuario");
        es.put("users.list.title", "Usuarios del sistema");
        es.put("passengers.form.title", "Registro de pasajero");
        es.put("drivers.form.title", "Registro de conductor");
        es.put("drivers.label.fullname", "Nombre completo:");
        es.put("vehicles.form.title", "Registro de vehículo");
        es.put("common.save", "Guardar");
        es.put("common.delete", "Eliminar");
        es.put("common.clear", "Limpiar");
        es.put("common.na", "N/D");
        es.put("servicos.warn.select.os", "Seleccione un servicio vinculado a una OS.");
        es.put("servicos.error.os.notfound", "OS no encontrada.");
        es.put("servicos.warn.no.transfers", "La OS no tiene transferencias vinculadas para generar el PDF.");
        es.put("servicos.pdf.saved", "PDF guardado en:\n{path}\n\n¿Abrir el archivo?");
        es.put("servicos.pdf.title", "PDF generado");
        es.put("servicos.pdf.error", "Error al generar PDF: {msg}");
        es.put("servicos.error.transfer.notfound", "Transferencia no encontrada.");
        es.put("servicos.status.updated", "Estado actualizado a: {status}");
        es.put("servicos.error.update", "Error al actualizar: {msg}");
        es.put("servicos.delete.confirm", "¿Eliminar la transferencia #{id} de la base de datos?\nEsta acción no se puede deshacer.");
        es.put("servicos.delete.success", "Servicio eliminado de la base.");
        es.put("servicos.error.delete", "Error al eliminar: {msg}");
        es.put("login.button.offline", "Entrar sin conexión (datos guardados)");
        es.put("login.offline.need.user", "Indique el usuario para cargar la caché local.");
        es.put("login.offline.no.cache", "No hay datos offline para este usuario. Conéctese la primera vez.");
        es.put("login.offline.no.network", "Sin red — intentando modo offline...");
        es.put("offline.status.online", "🟢 En línea — datos sincronizados");
        es.put("offline.status.offline", "🟠 Modo offline — consultando datos del dispositivo");
        es.put("offline.status.empty", "⚠ Sin datos locales. Conéctese y abra Servicios una vez.");
        es.put("offline.status.synced", "última sync: {when}");
        es.put("offline.status.pending", "{n} cambio(s) pendiente(s) de envío");

        translations.put(Language.PORTUGUESE, pt);
        translations.put(Language.ENGLISH, en);
        translations.put(Language.SPANISH, es);
    }

    public String translate(String key) {
        Map<String, String> currentTranslations = translations.get(currentLanguage);
        return currentTranslations.getOrDefault(key, key);
    }

    public String translate(String key, String defaultValue) {
        Map<String, String> currentTranslations = translations.get(currentLanguage);
        return currentTranslations.getOrDefault(key, defaultValue);
    }

    public String translate(String key, Map<String, String> params) {
        String text = translate(key);
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                text = text.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }
        return text;
    }

    public String translateStatus(StatusTransfer status) {
        if (status == null) return "";
        return translate("status.transfer." + status.name());
    }

    public Language getCurrentLanguage() {
        return currentLanguage;
    }

    public void setLanguage(Language language) {
        if (this.currentLanguage != language) {
            this.currentLanguage = language;
            notifyLanguageChanged();
        }
    }

    public void addLanguageChangeListener(LanguageChangeListener listener) {
        listeners.add(listener);
    }

    public void removeLanguageChangeListener(LanguageChangeListener listener) {
        listeners.remove(listener);
    }

    private void notifyLanguageChanged() {
        for (LanguageChangeListener listener : listeners) {
            listener.onLanguageChanged(currentLanguage);
        }
    }

    public interface LanguageChangeListener {
        void onLanguageChanged(Language newLanguage);
    }
}