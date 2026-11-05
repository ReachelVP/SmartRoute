package cr.ac.una.smartroute.presentation.admin;

import cr.ac.una.smartroute.business.LugarAdminService;
import cr.ac.una.smartroute.business.exception.LugarInexistenteOInactivoException;
import cr.ac.una.smartroute.data.entity.Lugar;
import cr.ac.una.smartroute.data.specification.LugarSpecifications;
import cr.ac.una.smartroute.presentation.admin.dto.LugarFormulario;
import jakarta.validation.Valid;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Módulo administrativo Thymeleaf (Laboratorio 6), caso de uso 1 de 2: catálogo de lugares.
 * Reutiliza {@link LugarAdminService}, la misma capa de negocio que ya consume la API
 * {@code /api/v1/lugares} del Laboratorio 5 — el único punto nuevo es esta presentación con vistas
 * del lado del servidor en vez de JSON. Protegido por sesión + rol ADMIN (ver
 * {@code AdminSecurityConfig}), con CSRF activo (a diferencia de la API, que es stateless y no lo
 * necesita) y patrón Post/Redirect/Get: cada POST exitoso termina en un redirect al listado, nunca
 * en un render directo, para que recargar la página no reenvíe el formulario.
 */
@Controller
@RequestMapping("/admin/lugares")
public class LugarAdminController {

    private final LugarAdminService lugarAdminService;

    public LugarAdminController(LugarAdminService lugarAdminService) {
        this.lugarAdminService = lugarAdminService;
    }

    @GetMapping
    public String listar(
            @RequestParam(required = false) Long provinciaId,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        Page<Lugar> pagina = lugarAdminService.listarPaginado(
                LugarSpecifications.conFiltrosOpcionales(provinciaId, categoriaId, null, null),
                PageRequest.of(page, 10, Sort.by("nombre").ascending()));

        model.addAttribute("pagina", pagina);
        model.addAttribute("provincias", lugarAdminService.listarProvincias());
        model.addAttribute("categorias", lugarAdminService.listarCategorias());
        model.addAttribute("provinciaId", provinciaId);
        model.addAttribute("categoriaId", categoriaId);
        return "admin/lugares/lista";
    }

    @GetMapping("/nuevo")
    public String formularioNuevo(Model model) {
        model.addAttribute("lugarFormulario", new LugarFormulario());
        model.addAttribute("provincias", lugarAdminService.listarProvincias());
        model.addAttribute("categorias", lugarAdminService.listarCategorias());
        model.addAttribute("esNuevo", true);
        return "admin/lugares/formulario";
    }

    @PostMapping("/nuevo")
    public String crear(
            @Valid @ModelAttribute("lugarFormulario") LugarFormulario formulario,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("provincias", lugarAdminService.listarProvincias());
            model.addAttribute("categorias", lugarAdminService.listarCategorias());
            model.addAttribute("esNuevo", true);
            return "admin/lugares/formulario";
        }

        Lugar creado = lugarAdminService.crear(
                formulario.getNombre(),
                formulario.getDescripcion(),
                formulario.getProvinciaId(),
                formulario.getCategoriaId(),
                formulario.getCostoEstimado(),
                formulario.getNivelAccesibilidad(),
                formulario.isRequiereTransportePropio());

        redirectAttributes.addFlashAttribute("mensaje", "Lugar \"" + creado.getNombre() + "\" creado.");
        return "redirect:/admin/lugares";
    }

    @GetMapping("/{id}/editar")
    public String formularioEditar(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Lugar lugar = lugarAdminService.obtener(id);
            model.addAttribute("lugarFormulario", LugarFormulario.desde(lugar));
            model.addAttribute("provincias", lugarAdminService.listarProvincias());
            model.addAttribute("categorias", lugarAdminService.listarCategorias());
            model.addAttribute("esNuevo", false);
            model.addAttribute("id", id);
            return "admin/lugares/formulario";
        } catch (LugarInexistenteOInactivoException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/admin/lugares";
        }
    }

    @PostMapping("/{id}/editar")
    public String actualizar(
            @PathVariable Long id,
            @Valid @ModelAttribute("lugarFormulario") LugarFormulario formulario,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("provincias", lugarAdminService.listarProvincias());
            model.addAttribute("categorias", lugarAdminService.listarCategorias());
            model.addAttribute("esNuevo", false);
            model.addAttribute("id", id);
            return "admin/lugares/formulario";
        }

        try {
            Lugar actualizado = lugarAdminService.actualizar(
                    id,
                    formulario.getNombre(),
                    formulario.getDescripcion(),
                    formulario.getProvinciaId(),
                    formulario.getCategoriaId(),
                    formulario.getCostoEstimado(),
                    formulario.getNivelAccesibilidad(),
                    formulario.isRequiereTransportePropio(),
                    formulario.getVersion());
            redirectAttributes.addFlashAttribute("mensaje", "Lugar \"" + actualizado.getNombre() + "\" actualizado.");
        } catch (LugarInexistenteOInactivoException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        } catch (OptimisticLockingFailureException ex) {
            // Bloqueo optimista (Lab 7): alguien más guardó un cambio sobre este mismo lugar
            // entre que esta persona abrió el formulario y lo envió.
            redirectAttributes.addFlashAttribute(
                    "error", "Alguien más editó este lugar mientras tanto. Recargá la página e intentá de nuevo.");
        }
        return "redirect:/admin/lugares";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            lugarAdminService.eliminar(id);
            redirectAttributes.addFlashAttribute("mensaje", "Lugar eliminado (baja lógica).");
        } catch (LugarInexistenteOInactivoException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/lugares";
    }
}
