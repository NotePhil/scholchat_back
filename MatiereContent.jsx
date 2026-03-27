import React, { useState, useEffect } from "react";
import {
  BookOpen,
  Plus,
  Search,
  RefreshCw,
  AlertCircle,
  CheckCircle,
  Loader,
  X,
  Save,
  Edit,
  Trash2,
} from "lucide-react";
import { matiereService } from "../../../../../services/MatiereService";
import { useAuth } from "../../../../../hooks/useAuth";

const MatiereContent = ({ isDark, currentTheme, themes, colorSchemes }) => {
  const [matieres, setMatieres] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState("");
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [selectedMatiere, setSelectedMatiere] = useState(null);
  const [message, setMessage] = useState({ text: "", type: "" });
  const [saving, setSaving] = useState(false);

  const [formData, setFormData] = useState({
    nom: "",
    description: "",
  });

  const { isAdmin, isProfessor } = useAuth();
  const canManage = isAdmin || isProfessor;

  const bgClass = isDark ? "bg-gray-900" : "bg-gray-50";
  const cardBgClass = isDark ? "bg-gray-800" : "bg-white";
  const textClass = isDark ? "text-white" : "text-gray-900";
  const textSecondaryClass = isDark ? "text-gray-300" : "text-gray-600";
  const borderClass = isDark ? "border-gray-700" : "border-gray-200";
  const inputBgClass = isDark ? "bg-gray-700" : "bg-gray-50";
  const inputTextClass = isDark ? "text-white" : "text-gray-900";

  useEffect(() => {
    if (canManage) {
      loadMatieres();
    }
  }, [canManage]);

  const loadMatieres = async () => {
    try {
      setLoading(true);
      const data = await matiereService.getAllMatieres();
      setMatieres(Array.isArray(data) ? data : []);
      setMessage({ text: "", type: "" });
    } catch (error) {
      console.error("Error loading matieres:", error);
      setMessage({ text: "Erreur lors du chargement des matières", type: "error" });
      setMatieres([]);
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = async () => {
    if (!formData.nom.trim()) {
      setMessage({ text: "Le nom de la matière est requis", type: "error" });
      return;
    }

    try {
      setSaving(true);
      await matiereService.createMatiere(formData);
      setMessage({ text: "Matière créée avec succès", type: "success" });
      setShowCreateModal(false);
      resetForm();
      await loadMatieres();
    } catch (error) {
      console.error("Error creating matiere:", error);
      setMessage({ text: error.message || "Erreur lors de la création", type: "error" });
    } finally {
      setSaving(false);
    }
  };

  const handleEdit = async () => {
    if (!formData.nom.trim()) {
      setMessage({ text: "Le nom de la matière est requis", type: "error" });
      return;
    }

    try {
      setSaving(true);
      await matiereService.updateMatiere(selectedMatiere.id, formData);
      setMessage({ text: "Matière modifiée avec succès", type: "success" });
      setShowEditModal(false);
      resetForm();
      await loadMatieres();
    } catch (error) {
      console.error("Error updating matiere:", error);
      setMessage({ text: error.message || "Erreur lors de la modification", type: "error" });
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async () => {
    try {
      setSaving(true);
      await matiereService.deleteMatiere(selectedMatiere.id);
      setMessage({ text: "Matière supprimée avec succès", type: "success" });
      setShowDeleteModal(false);
      resetForm();
      await loadMatieres();
    } catch (error) {
      console.error("Error deleting matiere:", error);
      setMessage({ text: error.message || "Erreur lors de la suppression", type: "error" });
    } finally {
      setSaving(false);
    }
  };

  const openEditModal = (matiere) => {
    setSelectedMatiere(matiere);
    setFormData({
      nom: matiere.nom,
      description: matiere.description || "",
    });
    setShowEditModal(true);
  };

  const openDeleteModal = (matiere) => {
    setSelectedMatiere(matiere);
    setShowDeleteModal(true);
  };

  const resetForm = () => {
    setFormData({
      nom: "",
      description: "",
    });
    setSelectedMatiere(null);
  };

  const closeModals = () => {
    setShowCreateModal(false);
    setShowEditModal(false);
    setShowDeleteModal(false);
    resetForm();
  };

  const filteredMatieres = matieres.filter(matiere =>
    matiere.nom?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    matiere.description?.toLowerCase().includes(searchTerm.toLowerCase())
  );

  if (!canManage) {
    return (
      <div className={`min-h-screen ${bgClass} flex items-center justify-center p-6`}>
        <div className={`${cardBgClass} rounded-2xl shadow-lg border ${borderClass} p-8 text-center max-w-md`}>
          <AlertCircle className="w-16 h-16 text-red-500 mx-auto mb-4" />
          <h2 className={`text-2xl font-bold ${textClass} mb-2`}>Accès Restreint</h2>
          <p className={textSecondaryClass}>
            Vous n'avez pas les permissions nécessaires pour accéder à cette section.
            Seuls les administrateurs et professeurs peuvent gérer les matières.
          </p>
        </div>
      </div>
    );
  }

  return (
    <div className={`min-h-screen ${bgClass} p-6`}>
      <div className="max-w-7xl mx-auto">
        {/* Header */}
        <div className={`${cardBgClass} rounded-2xl shadow-lg border ${borderClass} p-6 mb-6`}>
          <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
            <div className="flex items-center gap-4">
              <div className={`w-12 h-12 rounded-xl bg-gradient-to-br ${colorSchemes[currentTheme]?.gradient || "from-blue-500 to-blue-600"} flex items-center justify-center shadow-lg`}>
                <BookOpen className="w-6 h-6 text-white" />
              </div>
              <div>
                <h1 className={`text-3xl font-bold ${textClass}`}>Gestion des Matières</h1>
                <p className={textSecondaryClass}>Gérez les matières scolaires de votre établissement</p>
              </div>
            </div>
            {canManage && (
              <button
                onClick={() => setShowCreateModal(true)}
                className={`flex items-center gap-2 px-6 py-3 bg-gradient-to-r ${colorSchemes[currentTheme]?.gradient || "from-blue-500 to-blue-600"} text-white rounded-xl font-semibold shadow-lg hover:shadow-xl transition-all duration-300 transform hover:scale-105`}
              >
                <Plus className="w-5 h-5" />
                Nouvelle Matière
              </button>
            )}
          </div>
        </div>

        {/* Message */}
        {message.text && (
          <div className={`mb-6 p-4 rounded-xl border flex items-center gap-3 ${
            message.type === "success"
              ? `bg-green-50 border-green-200 text-green-700 ${isDark ? "dark:bg-green-900/20 dark:border-green-800 dark:text-green-400" : ""}`
              : `bg-red-50 border-red-200 text-red-700 ${isDark ? "dark:bg-red-900/20 dark:border-red-800 dark:text-red-400" : ""}`
          }`}>
            {message.type === "success" ? (
              <CheckCircle className="w-5 h-5" />
            ) : (
              <AlertCircle className="w-5 h-5" />
            )}
            <span className="font-medium">{message.text}</span>
            <button
              onClick={() => setMessage({ text: "", type: "" })}
              className="ml-auto hover:opacity-70"
            >
              <X className="w-4 h-4" />
            </button>
          </div>
        )}

        {/* Search */}
        <div className={`${cardBgClass} rounded-2xl shadow-lg border ${borderClass} p-6 mb-6`}>
          <div className="flex flex-col md:flex-row gap-4">
            <div className="flex-1 relative">
              <Search className={`absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 ${textSecondaryClass}`} />
              <input
                type="text"
                placeholder="Rechercher une matière..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className={`w-full pl-10 pr-4 py-3 rounded-xl border ${borderClass} ${inputBgClass} ${inputTextClass} focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-300`}
              />
            </div>
            <button
              onClick={loadMatieres}
              disabled={loading}
              className={`flex items-center gap-2 px-4 py-3 border ${borderClass} rounded-xl ${textClass} hover:${isDark ? "bg-gray-700" : "bg-gray-50"} transition-colors`}
            >
              <RefreshCw className={`w-4 h-4 ${loading ? "animate-spin" : ""}`} />
              Actualiser
            </button>
          </div>
        </div>

        {/* Content */}
        <div className={`${cardBgClass} rounded-2xl shadow-lg border ${borderClass} overflow-hidden`}>
          {loading ? (
            <div className="p-12 text-center">
              <Loader className="w-8 h-8 animate-spin text-blue-600 mx-auto mb-4" />
              <p className={textSecondaryClass}>Chargement des matières...</p>
            </div>
          ) : filteredMatieres.length === 0 ? (
            <div className="p-12 text-center">
              <BookOpen className={`w-16 h-16 ${textSecondaryClass} mx-auto mb-4`} />
              <h3 className={`text-xl font-semibold ${textClass} mb-2`}>
                {searchTerm ? "Aucune matière trouvée" : "Aucune matière"}
              </h3>
              <p className={textSecondaryClass}>
                {searchTerm 
                  ? "Essayez de modifier vos critères de recherche"
                  : "Commencez par créer votre première matière"
                }
              </p>
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead className={`${isDark ? "bg-gray-700" : "bg-gray-50"} border-b ${borderClass}`}>
                  <tr>
                    <th className={`px-6 py-4 text-left text-sm font-semibold ${textClass}`}>Matière</th>
                    <th className={`px-6 py-4 text-left text-sm font-semibold ${textClass}`}>Description</th>
                    <th className={`px-6 py-4 text-center text-sm font-semibold ${textClass}`}>Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-200 dark:divide-gray-700">
                  {filteredMatieres.map((matiere, index) => (
                    <tr key={index} className={`hover:${isDark ? "bg-gray-700" : "bg-gray-50"} transition-colors`}>
                      <td className="px-6 py-4">
                        <span className={`font-medium ${textClass}`}>{matiere.nom}</span>
                      </td>
                      <td className="px-6 py-4">
                        <span className={textSecondaryClass}>{matiere.description || "Aucune description"}</span>
                      </td>
                      <td className="px-6 py-4">
                        <div className="flex items-center justify-center gap-2">
                          <button
                            onClick={() => openEditModal(matiere)}
                            className="p-2 text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                            title="Modifier"
                          >
                            <Edit className="w-4 h-4" />
                          </button>
                          <button
                            onClick={() => openDeleteModal(matiere)}
                            className="p-2 text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                            title="Supprimer"
                          >
                            <Trash2 className="w-4 h-4" />
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>

      {/* Create Modal */}
      {showCreateModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className={`${cardBgClass} rounded-2xl shadow-xl w-full max-w-md`}>
            <div className={`p-6 border-b ${borderClass}`}>
              <div className="flex items-center justify-between">
                <h2 className={`text-xl font-bold ${textClass}`}>Nouvelle Matière</h2>
                <button
                  onClick={closeModals}
                  className={`p-2 rounded-lg ${textSecondaryClass} hover:${isDark ? "bg-gray-700" : "bg-gray-100"} transition-colors`}
                >
                  <X className="w-5 h-5" />
                </button>
              </div>
            </div>
            <div className="p-6 space-y-4">
              <div>
                <label className={`block text-sm font-semibold ${textClass} mb-2`}>
                  Nom de la matière *
                </label>
                <input
                  type="text"
                  value={formData.nom}
                  onChange={(e) => setFormData({ ...formData, nom: e.target.value })}
                  className={`w-full px-4 py-3 rounded-xl border ${borderClass} ${inputBgClass} ${inputTextClass} focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-300`}
                  placeholder="Ex: Mathématiques"
                />
              </div>
              <div>
                <label className={`block text-sm font-semibold ${textClass} mb-2`}>
                  Description
                </label>
                <textarea
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                  rows={3}
                  className={`w-full px-4 py-3 rounded-xl border ${borderClass} ${inputBgClass} ${inputTextClass} focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-300 resize-none`}
                  placeholder="Description de la matière..."
                />
              </div>
            </div>
            <div className={`p-6 border-t ${borderClass} flex justify-end gap-3`}>
              <button
                onClick={closeModals}
                className={`px-6 py-3 border ${borderClass} rounded-xl ${textClass} hover:${isDark ? "bg-gray-700" : "bg-gray-50"} transition-colors`}
              >
                Annuler
              </button>
              <button
                onClick={handleCreate}
                disabled={saving || !formData.nom.trim()}
                className={`flex items-center gap-2 px-6 py-3 bg-gradient-to-r ${colorSchemes[currentTheme]?.gradient || "from-blue-500 to-blue-600"} text-white rounded-xl font-semibold shadow-lg hover:shadow-xl transition-all duration-300 disabled:opacity-50`}
              >
                {saving ? (
                  <Loader className="w-4 h-4 animate-spin" />
                ) : (
                  <Save className="w-4 h-4" />
                )}
                {saving ? "Création..." : "Créer"}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Edit Modal */}
      {showEditModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className={`${cardBgClass} rounded-2xl shadow-xl w-full max-w-md`}>
            <div className={`p-6 border-b ${borderClass}`}>
              <div className="flex items-center justify-between">
                <h2 className={`text-xl font-bold ${textClass}`}>Modifier la Matière</h2>
                <button
                  onClick={closeModals}
                  className={`p-2 rounded-lg ${textSecondaryClass} hover:${isDark ? "bg-gray-700" : "bg-gray-100"} transition-colors`}
                >
                  <X className="w-5 h-5" />
                </button>
              </div>
            </div>
            <div className="p-6 space-y-4">
              <div>
                <label className={`block text-sm font-semibold ${textClass} mb-2`}>
                  Nom de la matière *
                </label>
                <input
                  type="text"
                  value={formData.nom}
                  onChange={(e) => setFormData({ ...formData, nom: e.target.value })}
                  className={`w-full px-4 py-3 rounded-xl border ${borderClass} ${inputBgClass} ${inputTextClass} focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-300`}
                  placeholder="Ex: Mathématiques"
                />
              </div>
              <div>
                <label className={`block text-sm font-semibold ${textClass} mb-2`}>
                  Description
                </label>
                <textarea
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                  rows={3}
                  className={`w-full px-4 py-3 rounded-xl border ${borderClass} ${inputBgClass} ${inputTextClass} focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-300 resize-none`}
                  placeholder="Description de la matière..."
                />
              </div>
            </div>
            <div className={`p-6 border-t ${borderClass} flex justify-end gap-3`}>
              <button
                onClick={closeModals}
                className={`px-6 py-3 border ${borderClass} rounded-xl ${textClass} hover:${isDark ? "bg-gray-700" : "bg-gray-50"} transition-colors`}
              >
                Annuler
              </button>
              <button
                onClick={handleEdit}
                disabled={saving || !formData.nom.trim()}
                className={`flex items-center gap-2 px-6 py-3 bg-gradient-to-r ${colorSchemes[currentTheme]?.gradient || "from-blue-500 to-blue-600"} text-white rounded-xl font-semibold shadow-lg hover:shadow-xl transition-all duration-300 disabled:opacity-50`}
              >
                {saving ? (
                  <Loader className="w-4 h-4 animate-spin" />
                ) : (
                  <Save className="w-4 h-4" />
                )}
                {saving ? "Modification..." : "Modifier"}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Delete Modal */}
      {showDeleteModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className={`${cardBgClass} rounded-2xl shadow-xl w-full max-w-md`}>
            <div className={`p-6 border-b ${borderClass}`}>
              <div className="flex items-center justify-between">
                <h2 className={`text-xl font-bold ${textClass}`}>Supprimer la Matière</h2>
                <button
                  onClick={closeModals}
                  className={`p-2 rounded-lg ${textSecondaryClass} hover:${isDark ? "bg-gray-700" : "bg-gray-100"} transition-colors`}
                >
                  <X className="w-5 h-5" />
                </button>
              </div>
            </div>
            <div className="p-6">
              <div className="flex items-center gap-4 mb-4">
                <div className="w-12 h-12 rounded-full bg-red-100 flex items-center justify-center">
                  <AlertCircle className="w-6 h-6 text-red-600" />
                </div>
                <div>
                  <h3 className={`text-lg font-semibold ${textClass}`}>Confirmer la suppression</h3>
                  <p className={textSecondaryClass}>Cette action est irréversible.</p>
                </div>
              </div>
              <p className={textSecondaryClass}>
                Êtes-vous sûr de vouloir supprimer la matière <strong>{selectedMatiere?.nom}</strong> ?
              </p>
            </div>
            <div className={`p-6 border-t ${borderClass} flex justify-end gap-3`}>
              <button
                onClick={closeModals}
                className={`px-6 py-3 border ${borderClass} rounded-xl ${textClass} hover:${isDark ? "bg-gray-700" : "bg-gray-50"} transition-colors`}
              >
                Annuler
              </button>
              <button
                onClick={handleDelete}
                disabled={saving}
                className="flex items-center gap-2 px-6 py-3 bg-red-600 text-white rounded-xl font-semibold shadow-lg hover:shadow-xl transition-all duration-300 disabled:opacity-50"
              >
                {saving ? (
                  <Loader className="w-4 h-4 animate-spin" />
                ) : (
                  <Trash2 className="w-4 h-4" />
                )}
                {saving ? "Suppression..." : "Supprimer"}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default MatiereContent;