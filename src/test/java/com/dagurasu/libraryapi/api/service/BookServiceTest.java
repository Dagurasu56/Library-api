package com.dagurasu.libraryapi.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.dagurasu.libraryapi.api.model.entity.Book;
import com.dagurasu.libraryapi.api.model.repository.BookRepository;
import com.dagurasu.libraryapi.api.service.imp.BookServiceImpl;
import com.dagurasu.libraryapi.exception.BusinessException;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
public class BookServiceTest {

	BookService service;

	@MockBean
	BookRepository repository;

	@BeforeEach
	public void init() {
		this.service = new BookServiceImpl(repository);
	}

	@Test
	@DisplayName("Deve salvar um livro.")
	public void saveBookTest() {

		Book book = createValidBook();
		Mockito.when(repository.existsByIsbn(Mockito.anyString())).thenReturn(false);
		Mockito.when(repository.save(book))
				.thenReturn(Book.builder().id(1l).isbn("123").title("As Aventuras").author("Fulano").build());

		Book savedBook = service.save(book);

		assertThat(savedBook.getId()).isNotNull();
		assertThat(savedBook.getIsbn()).isEqualTo("123");
		assertThat(savedBook.getTitle()).isEqualTo("As Aventuras");
		assertThat(savedBook.getAuthor()).isEqualTo("Fulano");
	}

	@Test
	@DisplayName("Deve lançar erro de neǵocio ao tentar salvar um livro com isbn duplicado.")
	public void shouldNotSaveBookWithDuplicatedISBN() {

		Book book = createValidBook();
		Mockito.when(repository.existsByIsbn(Mockito.anyString())).thenReturn(true);

		Throwable exception = Assertions.catchThrowable(() -> service.save(book));
		assertThat(exception).isInstanceOf(BusinessException.class).hasMessage("Isbn já cadastrado.");

		Mockito.verify(repository, Mockito.never()).save(book);

	}

	@Test
	@DisplayName("Deve obter um livro por id.")
	public void getIdByTest() {
		Long id = 1l;

		Book book = createValidBook();
		book.setId(id);

		Mockito.when(repository.findById(id)).thenReturn(Optional.of(book));

		Optional<Book> foundBook = service.getById(id);

		assertThat(foundBook.isPresent()).isTrue();
		assertThat(foundBook.get().getId()).isEqualTo(id);
		assertThat(foundBook.get().getAuthor()).isEqualTo(book.getAuthor());
		assertThat(foundBook.get().getTitle()).isEqualTo(book.getTitle());
		assertThat(foundBook.get().getIsbn()).isEqualTo(book.getIsbn());
	}

	@Test
	@DisplayName("Deve retornar vazio ao obter um livro por id quando ele não existe na base.")
	public void bookNotFoundIdByTest() {
		Long id = 1l;

		Mockito.when(repository.findById(id)).thenReturn(Optional.empty());

		Optional<Book> book = service.getById(id);

		assertThat(book.isPresent()).isFalse();
	}

	@Test
	@DisplayName("Deve deletar um livro.")
	public void deleteBookTest() {
		Book book = Book.builder().id(1l).isbn("123").author("Fulano").title("As Aventuras").build();

		org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> service.delete(book));

		Mockito.verify(repository, Mockito.times(1)).delete(book);
	}

	@Test
	@DisplayName("Deve ocorrer erro ao tentar deletar um livro inexistente.")
	public void deleteIvalidBookTest() {
		Book book = new Book();

		org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> service.delete(book));

		Mockito.verify(repository, Mockito.never()).delete(book);
	}

	@Test
	@DisplayName("Deve ocorrer erro ao tentar atualizar um livro inexistente.")
	public void updateInvalidBookTest() {
		Book book = new Book();

		org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> service.update(book));

		Mockito.verify(repository, Mockito.never()).save(book);
	}

	@Test
	@DisplayName("Deve atualizar um livro existente.")
	public void updateBookTest() {
		Long id = 1l;

		Book updatingBook = Book.builder().id(id).build();

		Book updatedBook = createValidBook();
		updatedBook.setId(id);

		Mockito.when(repository.save(updatingBook)).thenReturn(updatedBook);

		Book book = service.update(updatingBook);

		assertThat(book.getId()).isEqualTo(updatedBook.getId());
		assertThat(book.getTitle()).isEqualTo(updatedBook.getTitle());
		assertThat(book.getAuthor()).isEqualTo(updatedBook.getAuthor());
		assertThat(book.getIsbn()).isEqualTo(updatedBook.getIsbn());
	}

	@Test
	@SuppressWarnings("unchecked")
	@DisplayName("Deve filtrar livros pela propriedade.")
	public void findBookTest() {

		Book book = createValidBook();

		PageRequest pageRequest = PageRequest.of(0, 10);

		List<Book> lista = Arrays.asList(book);
		Page<Book> page = new PageImpl<Book>(lista, pageRequest, 1);
		Mockito.when(repository.findAll(Mockito.any(Example.class), Mockito.any(PageRequest.class))).thenReturn(page);

		Page<Book> result = service.find(book, pageRequest);

		assertThat(result.getTotalElements()).isEqualTo(1);
		assertThat(result.getContent()).isEqualTo(lista);
		assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
		assertThat(result.getPageable().getPageSize()).isEqualTo(10);

	}

	@Test
	@DisplayName("Deve obter um livro pelo isbn.")
	public void getBookByIsbn() {

		String isbn = "1230";
		when(repository.findByIsbn(isbn)).thenReturn(Optional.of(Book.builder().id(1l).isbn(isbn).build()));

		Optional<Book> book = service.getBookByIsbn(isbn);

		assertThat(book.isPresent()).isTrue();
		assertThat(book.get().getId()).isEqualTo(1l);
		assertThat(book.get().getIsbn()).isEqualTo(isbn);

		verify(repository, times(1)).findByIsbn(isbn);
	}

	private Book createValidBook() {
		return Book.builder().isbn("123").author("Fulano").title("As Aventuras").build();
	}

}
