package curso.springboot.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import curso.springboot.model.Pessoa;
import curso.springboot.model.Telefone;
import curso.springboot.repository.PessoaRepository;
import curso.springboot.repository.ProfissaoRepository;
import curso.springboot.repository.TelefoneRepository;

@Controller
public class PessoaController {

	@Autowired // injeçao de dependecia para ter acesso aos recursos do repository
	private TelefoneRepository telefoneRepository;

	@Autowired
	private PessoaRepository pessoaRepository;

	@Autowired
	private ReportUtil reportUtil;

	@Autowired
	private ProfissaoRepository profissaoRepository;
	
	@RequestMapping(method = RequestMethod.GET, value = "**/cadastropessoa")
	public ModelAndView inicio() {
		ModelAndView modelAndView = new ModelAndView("cadastro/cadastropessoa");
		modelAndView.addObject("pessoaobj", new Pessoa());
		
		/*Carregando a lista por paginação*/
		Iterable<Pessoa> pessoasIt = pessoaRepository.findAll(PageRequest.of(0, 5, Sort.by("nome")));
		modelAndView.addObject("pessoas", pessoasIt);
		modelAndView.addObject("profissoes",profissaoRepository.findAll());// na hora que abrir a tela ira carregar as profissoes
		return modelAndView;
	}

	/*Metodo para carrega as paginas Ex.pagina 1 , 2 , 3 , 4 etc*/
	@GetMapping("/pessoaspag")
	public ModelAndView carregaPessoaPorPaginaçao(@PageableDefault(size = 5) Pageable pageable, ModelAndView model) {

		Page<Pessoa> pagePessoa = pessoaRepository.findAll(pageable);
		model.addObject("pessoas", pagePessoa);
		model.addObject("pessoaobj", new Pessoa());
		model.setViewName("cadastro/cadastropessoa");
		return model;
	}
	
	
	@RequestMapping(method = RequestMethod.POST, value = "**/salvarpessoa", consumes = {"multipart/form-data"}) //consumes diz que o formulário vai salvar um arquivo
	public ModelAndView salvar(@Valid Pessoa pessoa, BindingResult bindingResult, final MultipartFile file) throws IOException { // @valid e BindingResult é para validaçoes dos campos
																					

		pessoa.setTelefones(telefoneRepository.getTelefones(pessoa.getId())); // carregando a lista de telefone do
																				// usuario antes de salvar

		// validaçoes dos campos inicio
		if (bindingResult.hasErrors()) {
			ModelAndView modelAndView = new ModelAndView("cadastro/cadastropessoa");
			modelAndView.addObject("pessoas", pessoaRepository.findAll(PageRequest.of(0, 5, Sort.by("nome"))));
			modelAndView.addObject("pessoaobj", pessoa);

			List<String> msg = new ArrayList<String>();
			for (ObjectError objectError : bindingResult.getAllErrors()) {
				msg.add(objectError.getDefaultMessage()); // defaultmessage vem das anotaçoes e outras
			}
			modelAndView.addObject("msg", msg);
			modelAndView.addObject("profissoes",profissaoRepository.findAll());// na hora que abrir a tela ira carregar as profissoes
			return modelAndView;
		}
		if (file.getSize() > 0) { // cadastrando o currículo
			pessoa.setCurriculo(file.getBytes());
			pessoa.setTipoFileCurriculo(file.getContentType());
			pessoa.setNomeFileCurriculo(file.getOriginalFilename());
		
		} else {
			if (pessoa.getId() != null && pessoa.getId() > 0) { // mante o upload quando for editar
				
				Pessoa pessoaTemp = pessoaRepository.findById(pessoa.getId()).get();
				
				pessoa.setCurriculo(pessoaTemp.getCurriculo());
				pessoa.setTipoFileCurriculo(pessoaTemp.getTipoFileCurriculo());
				pessoa.setNomeFileCurriculo(pessoaTemp.getNomeFileCurriculo());
			}

		}
		
		// validaçoes dos campos termino

		pessoaRepository.save(pessoa);

		ModelAndView andView = new ModelAndView("cadastro/cadastropessoa");
		andView.addObject("pessoas", pessoaRepository.findAll(PageRequest.of(0, 5, Sort.by("nome"))));
		andView.addObject("pessoaobj", new Pessoa());
		return andView;

	}

	@RequestMapping(method = RequestMethod.GET, value = "/listapessoas")
	public ModelAndView pessoas() {
		ModelAndView andView = new ModelAndView("cadastro/cadastropessoa");
		andView.addObject("pessoas", pessoaRepository.findAll(PageRequest.of(0, 5, Sort.by("nome"))));
		andView.addObject("pessoaobj", new Pessoa());
		return andView;
	}

	@GetMapping("/editarpessoa/{idpessoa}") // mapear a url do html do "editar"
	public ModelAndView editar(@PathVariable("idpessoa") Long idpessoa) {
		java.util.Optional<Pessoa> pessoa = pessoaRepository.findById(idpessoa);
		ModelAndView modelAndView = new ModelAndView("cadastro/cadastropessoa");
		modelAndView.addObject("pessoaobj", pessoa.get());
		modelAndView.addObject("profissoes",profissaoRepository.findAll());// na hora que abrir a tela ira carregar as profissoes
		return modelAndView;

	}

	@GetMapping("/removerpessoa/{idpessoa}") // mapear a url do html do "editar"
	public ModelAndView excluir(@PathVariable("idpessoa") Long idpessoa) {

		pessoaRepository.deleteById(idpessoa);

		ModelAndView modelAndView = new ModelAndView("cadastro/cadastropessoa");
		modelAndView.addObject("pessoas", pessoaRepository.findAll(PageRequest.of(0, 5, Sort.by("nome"))));
		modelAndView.addObject("pessoaobj", new Pessoa());
		return modelAndView;

	}

	// metodo de pesquisar por nome com filtro de campo de sexo
	@PostMapping("**/pesquisarpessoa") // essa anotaçao mapea a url por post
	public ModelAndView pesquisar(@RequestParam("nomepesquisa") String nomepesquisa,
			@RequestParam("pesqsexo") String pesqsexo) {

		List<Pessoa> pessoas = new ArrayList<Pessoa>();

		if (pesqsexo != null && !pesqsexo.isEmpty()) {
			pessoas = pessoaRepository.findPessoasByNameSexo(nomepesquisa, pesqsexo);
		} else {
			pessoas = pessoaRepository.findPessoasByName(nomepesquisa);
		}

		ModelAndView modelAndView = new ModelAndView("cadastro/cadastropessoa");
		modelAndView.addObject("pessoas", pessoas);
		modelAndView.addObject("pessoaobj", new Pessoa());
		return modelAndView;
	}

	@GetMapping("**/baixarcurriculo/{idpessoa}")
	public void baixarcurriculo(@PathVariable("idpessoa") Long idpessoa,
			HttpServletResponse response) throws IOException {
		/* Consultar o objeto pessoa no banco de dados*/
		Pessoa pessoa = pessoaRepository.findById(idpessoa).get();
		if(pessoa.getCurriculo() != null) {
		
			/*Setar tamanho da resposta*/
		response.setContentLength(pessoa.getCurriculo().length);
			
		/*Tipo do arquivo para download*/
		response.setContentType(pessoa.getTipoFileCurriculo());
		
		/*Define o cabeçalho da resposta*/
		String headerKey = "Content-Disposition";
		String headerValue = String.format("attachment; filename=\"%s\"", pessoa.getNomeFileCurriculo());
		response.setHeader(headerKey, headerValue);

		/*Finaliza a resposta passando o arquivo*/
		response.getOutputStream().write(pessoa.getCurriculo());
		
		}
	}
	
	
	@GetMapping("**/pesquisarpessoa") // essa anotaçao mapea a url por post
	public void imprimePdf(@RequestParam("nomepesquisa") String nomepesquisa, @RequestParam("pesqsexo") String pesqsexo,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		List<Pessoa> pessoas = new ArrayList<Pessoa>();

		if (pesqsexo != null && !pesqsexo.isEmpty() && nomepesquisa != null && !nomepesquisa.isEmpty()) { // Busca por nome e sexo
			pessoas = pessoaRepository.findPessoasByNameSexo(nomepesquisa, pesqsexo);

		} else if (nomepesquisa != null && !nomepesquisa.isEmpty()) { // Busca somente por nome
			pessoas = pessoaRepository.findPessoasByName(nomepesquisa);

		} 
		
		else if (pesqsexo != null && !pesqsexo.isEmpty()) { // Busca somente por sexo
			pessoas = pessoaRepository.findPessoasBySexo(pesqsexo);

	}
		
		else {
			Iterable<Pessoa> iterator = pessoaRepository.findAll(); // Busca todos
			for (Pessoa pessoa : iterator) {
				pessoas.add(pessoa);
			}
		}
		/* Chamar o serviço que faz a geração do relatorio */
		byte[] pdf = reportUtil.gerarRelatorio(pessoas, "pessoa", request.getServletContext());
		
		/* Tamanho da resposta do navegador */
		response.setContentLength(pdf.length);

		/* Definir na resposta o tipo de arquivo */
		response.setContentType("application/octet-stream"); // consegue fazer qualquer tipo de download com essa
																// resposta ex. midia, pdf, imagem

		/* Definir o cabeçalho da resposta */
		String headerKey = "Content-Disposition";
		String headerValue = String.format("attachment; filename=\"%s\"", "relatorio.pdf");
		response.setHeader(headerKey, headerValue);

		/* Finaliza a resposta para o navegador */
		response.getOutputStream().write(pdf);
	}

	@GetMapping("/telefones/{idpessoa}") // mapear a url do html do "editar"
	public ModelAndView telefones(@PathVariable("idpessoa") Long idpessoa) {
		java.util.Optional<Pessoa> pessoa = pessoaRepository.findById(idpessoa);
		ModelAndView modelAndView = new ModelAndView("cadastro/telefones");
		modelAndView.addObject("pessoaobj", pessoa.get());
		modelAndView.addObject("telefones", telefoneRepository.getTelefones(idpessoa));
		return modelAndView;

	}

	@PostMapping("**/addfonePessoa/{pessoaid}")
	public ModelAndView addFonePessoa(Telefone telefone, @PathVariable("pessoaid") Long pessoaid) {

		Pessoa pessoa = pessoaRepository.findById(pessoaid).get();
		if (telefone != null && telefone.getNumero().isEmpty() || telefone.getTipo().isEmpty()) {

			ModelAndView modelAndView = new ModelAndView("cadastro/telefones");
			modelAndView.addObject("pessoaobj", pessoa);
			modelAndView.addObject("telefones", telefoneRepository.getTelefones(pessoaid));

			List<String> msg = new ArrayList<String>();
			if (telefone.getNumero().isEmpty()) {
				msg.add("Número deve ser informado");
			}
			if (telefone.getTipo().isEmpty()) {
				msg.add("Tipo deve ser informado");
			}
			modelAndView.addObject("msg", msg);
			return modelAndView;
		}
		ModelAndView modelAndView = new ModelAndView("cadastro/telefones");
		telefone.setPessoa(pessoa);
		telefoneRepository.save(telefone);
		modelAndView.addObject("pessoaobj", pessoa);
		modelAndView.addObject("telefones", telefoneRepository.getTelefones(pessoaid));
		return modelAndView;
	}

	@GetMapping("/removertelefone/{idtelefone}") // mapear a url do html do "editar"
	public ModelAndView removertelefone(@PathVariable("idtelefone") Long idtelefone) {

		Pessoa pessoa = telefoneRepository.findById(idtelefone).get().getPessoa(); // carreguei o objeto telefone junto
																					// com a pessoa onde tem o pai
		telefoneRepository.deleteById(idtelefone); // deletei o telefone correspondente

		ModelAndView modelAndView = new ModelAndView("cadastro/telefones"); // retorna para a mesma tela
		modelAndView.addObject("pessoaobj", pessoa);// passa o objeto pai para carregar na tela
		modelAndView.addObject("telefones", telefoneRepository.getTelefones(pessoa.getId()));// carrega o telefones
																								// novamente menos o
																								// telefone exluido
		return modelAndView;
	}
}