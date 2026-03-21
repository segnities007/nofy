# UI Development Guidelines

## Material 3 Usage Policy
- **Strict Prohibition**: Direct use of `androidx.compose.material3` components or `MaterialTheme` is strictly prohibited outside the `:platform:designsystem` module.
- **Mandatory Alternative**: All UI components (Atoms, Molecules) must be imported from the `:platform:designsystem` module.
- **Exception**: The `:platform:designsystem` module itself is the only place allowed to depend on and use Material 3 library components directly.
- **Review Requirement**: Any PR introducing direct Material 3 dependencies in feature or app modules will be rejected.
- **Build Enforcement**: `implementation(libs.androidx.compose.material3)` must not be added to any module other than `:platform:designsystem`.

## Compose Preview Policy
- **Mandatory Previews**: Every public or internal `@Composable` function that represents a UI component (Atom, Molecule, or Screen) MUST have at least one `@Preview` function associated with it.
- **Preview Placement**: Previews should be placed at the bottom of the same file as the component, marked as `private` if possible.
- **Theme Usage**: Use `NofyTheme` in all previews to ensure consistency with the app's design system.
- **Sample Data**: Use representative sample data for previews to demonstrate various states (e.g., loading, error, empty).
